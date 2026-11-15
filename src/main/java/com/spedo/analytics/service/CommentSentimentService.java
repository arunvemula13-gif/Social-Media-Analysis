package com.spedo.analytics.service;

import com.spedo.analytics.dto.PostCommentDto;
import com.spedo.analytics.dto.PostSentimentSummaryDto;
import com.spedo.analytics.entity.Post;
import com.spedo.analytics.entity.PostComment;
import com.spedo.analytics.repository.PostCommentRepository;
import com.spedo.analytics.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class CommentSentimentService {

    private static final Logger log = LoggerFactory.getLogger(CommentSentimentService.class);
    private static final Pattern WORD_SPLIT = Pattern.compile("[\\s,;:.!?\"'()\\[\\]{}]+");

    // Negative / Complaint Dictionary with weights
    private static final Map<String, Double> NEGATIVE_LEXICON = new HashMap<>();
    // Positive / Satisfaction Dictionary with weights
    private static final Map<String, Double> POSITIVE_LEXICON = new HashMap<>();
    // High-impact Emoji sentiment dictionary
    private static final Map<String, Double> EMOJI_LEXICON = new LinkedHashMap<>();
    // Negation words that invert sentiment
    private static final Set<String> NEGATION_WORDS = new HashSet<>(Arrays.asList(
            "not", "no", "never", "cannot", "cant", "can't", "don't", "dont", "doesn't", "doesnt",
            "won't", "wont", "isn't", "isnt", "hardly", "barely", "scarcely", "without"
    ));

    static {
        // High-impact Emojis
        EMOJI_LEXICON.put("🔥", 0.9);
        EMOJI_LEXICON.put("❤️", 0.9);
        EMOJI_LEXICON.put("😍", 0.9);
        EMOJI_LEXICON.put("👏", 0.8);
        EMOJI_LEXICON.put("💯", 0.85);
        EMOJI_LEXICON.put("🙌", 0.8);
        EMOJI_LEXICON.put("✨", 0.7);
        EMOJI_LEXICON.put("👍", 0.7);
        EMOJI_LEXICON.put("💖", 0.9);
        EMOJI_LEXICON.put("🤩", 0.9);
        EMOJI_LEXICON.put("🥰", 0.9);
        EMOJI_LEXICON.put("🎶", 0.6);
        EMOJI_LEXICON.put("🎵", 0.6);
        EMOJI_LEXICON.put("👌", 0.75);
        EMOJI_LEXICON.put("👑", 0.85);
        EMOJI_LEXICON.put("🚀", 0.85);
        EMOJI_LEXICON.put("💪", 0.75);
        EMOJI_LEXICON.put("💎", 0.85);
        EMOJI_LEXICON.put("🎯", 0.8);
        EMOJI_LEXICON.put("🙏", 0.7);

        EMOJI_LEXICON.put("🤮", -0.9);
        EMOJI_LEXICON.put("💩", -0.85);
        EMOJI_LEXICON.put("👎", -0.85);
        EMOJI_LEXICON.put("😡", -0.85);
        EMOJI_LEXICON.put("🤬", -0.9);
        EMOJI_LEXICON.put("🤡", -0.8);
        EMOJI_LEXICON.put("💔", -0.75);
        EMOJI_LEXICON.put("🤢", -0.85);
        EMOJI_LEXICON.put("😠", -0.8);
        EMOJI_LEXICON.put("📉", -0.75);
        EMOJI_LEXICON.put("⚠️", -0.6);

        // Negative / Bug / Complaint / Critique keywords
        NEGATIVE_LEXICON.put("broken", -0.9);
        NEGATIVE_LEXICON.put("crash", -0.95);
        NEGATIVE_LEXICON.put("crashes", -0.95);
        NEGATIVE_LEXICON.put("crashing", -0.95);
        NEGATIVE_LEXICON.put("bug", -0.8);
        NEGATIVE_LEXICON.put("bugs", -0.8);
        NEGATIVE_LEXICON.put("buggy", -0.85);
        NEGATIVE_LEXICON.put("glitch", -0.75);
        NEGATIVE_LEXICON.put("glitches", -0.75);
        NEGATIVE_LEXICON.put("freeze", -0.85);
        NEGATIVE_LEXICON.put("freezes", -0.85);
        NEGATIVE_LEXICON.put("refund", -0.9);
        NEGATIVE_LEXICON.put("scam", -1.0);
        NEGATIVE_LEXICON.put("fraud", -0.95);
        NEGATIVE_LEXICON.put("worst", -0.95);
        NEGATIVE_LEXICON.put("terrible", -0.9);
        NEGATIVE_LEXICON.put("horrible", -0.9);
        NEGATIVE_LEXICON.put("awful", -0.85);
        NEGATIVE_LEXICON.put("useless", -0.85);
        NEGATIVE_LEXICON.put("unusable", -0.9);
        NEGATIVE_LEXICON.put("fail", -0.8);
        NEGATIVE_LEXICON.put("failed", -0.85);
        NEGATIVE_LEXICON.put("failing", -0.85);
        NEGATIVE_LEXICON.put("disappointed", -0.8);
        NEGATIVE_LEXICON.put("disappointing", -0.8);
        NEGATIVE_LEXICON.put("hate", -0.85);
        NEGATIVE_LEXICON.put("waste", -0.8);
        NEGATIVE_LEXICON.put("slow", -0.6);
        NEGATIVE_LEXICON.put("lag", -0.65);
        NEGATIVE_LEXICON.put("lagging", -0.65);
        NEGATIVE_LEXICON.put("stuck", -0.7);
        NEGATIVE_LEXICON.put("error", -0.75);
        NEGATIVE_LEXICON.put("errors", -0.75);
        NEGATIVE_LEXICON.put("sucks", -0.85);
        NEGATIVE_LEXICON.put("pathetic", -0.9);
        NEGATIVE_LEXICON.put("trash", -0.85);
        NEGATIVE_LEXICON.put("unresolved", -0.75);
        NEGATIVE_LEXICON.put("bad", -0.6);
        NEGATIVE_LEXICON.put("poor", -0.65);
        NEGATIVE_LEXICON.put("problem", -0.5);
        NEGATIVE_LEXICON.put("problems", -0.55);
        NEGATIVE_LEXICON.put("boring", -0.75);
        NEGATIVE_LEXICON.put("bore", -0.7);
        NEGATIVE_LEXICON.put("cringe", -0.8);
        NEGATIVE_LEXICON.put("dislike", -0.7);
        NEGATIVE_LEXICON.put("overrated", -0.75);
        NEGATIVE_LEXICON.put("cheap", -0.65);
        NEGATIVE_LEXICON.put("clickbait", -0.85);
        NEGATIVE_LEXICON.put("copied", -0.7);
        NEGATIVE_LEXICON.put("ruined", -0.85);
        NEGATIVE_LEXICON.put("spoiled", -0.8);
        NEGATIVE_LEXICON.put("garbage", -0.85);
        NEGATIVE_LEXICON.put("annoying", -0.7);
        NEGATIVE_LEXICON.put("shame", -0.75);
        NEGATIVE_LEXICON.put("unsubscribed", -0.85);
        NEGATIVE_LEXICON.put("flop", -0.8);
        NEGATIVE_LEXICON.put("unwatchable", -0.9);
        NEGATIVE_LEXICON.put("stupid", -0.8);
        NEGATIVE_LEXICON.put("idiot", -0.85);
        NEGATIVE_LEXICON.put("nonsense", -0.75);
        NEGATIVE_LEXICON.put("rubbish", -0.8);
        NEGATIVE_LEXICON.put("clueless", -0.75);
        NEGATIVE_LEXICON.put("bookish", -0.6);
        NEGATIVE_LEXICON.put("dishonest", -0.85);
        NEGATIVE_LEXICON.put("cheat", -0.9);
        NEGATIVE_LEXICON.put("losses", -0.75);
        NEGATIVE_LEXICON.put("bearish", -0.65);

        // News & Current Events Negative / Alert Vocabulary
        NEGATIVE_LEXICON.put("warning", -0.7);
        NEGATIVE_LEXICON.put("danger", -0.85);
        NEGATIVE_LEXICON.put("dangerous", -0.85);
        NEGATIVE_LEXICON.put("cyclone", -0.85);
        NEGATIVE_LEXICON.put("storm", -0.8);
        NEGATIVE_LEXICON.put("flood", -0.85);
        NEGATIVE_LEXICON.put("floods", -0.85);
        NEGATIVE_LEXICON.put("accident", -0.9);
        NEGATIVE_LEXICON.put("collision", -0.85);
        NEGATIVE_LEXICON.put("death", -0.95);
        NEGATIVE_LEXICON.put("deaths", -0.95);
        NEGATIVE_LEXICON.put("dead", -0.95);
        NEGATIVE_LEXICON.put("killed", -0.95);
        NEGATIVE_LEXICON.put("injured", -0.85);
        NEGATIVE_LEXICON.put("crisis", -0.9);
        NEGATIVE_LEXICON.put("disaster", -0.95);
        NEGATIVE_LEXICON.put("damage", -0.75);
        NEGATIVE_LEXICON.put("loss", -0.75);
        NEGATIVE_LEXICON.put("inflation", -0.7);
        NEGATIVE_LEXICON.put("strike", -0.7);
        NEGATIVE_LEXICON.put("arrested", -0.75);
        NEGATIVE_LEXICON.put("crime", -0.85);
        NEGATIVE_LEXICON.put("threat", -0.8);
        NEGATIVE_LEXICON.put("severe", -0.75);
        NEGATIVE_LEXICON.put("deadly", -0.95);
        NEGATIVE_LEXICON.put("pollution", -0.7);
        NEGATIVE_LEXICON.put("collapse", -0.85);
        NEGATIVE_LEXICON.put("fake", -0.85);
        NEGATIVE_LEXICON.put("untrue", -0.8);
        NEGATIVE_LEXICON.put("misleading", -0.8);
        NEGATIVE_LEXICON.put("bias", -0.7);
        NEGATIVE_LEXICON.put("biased", -0.75);
        NEGATIVE_LEXICON.put("corrupt", -0.9);
        NEGATIVE_LEXICON.put("corruption", -0.9);

        // Positive / Satisfaction / Content Creator Praise keywords
        POSITIVE_LEXICON.put("love", 0.9);
        POSITIVE_LEXICON.put("loved", 0.85);
        POSITIVE_LEXICON.put("awesome", 0.9);
        POSITIVE_LEXICON.put("amazing", 0.9);
        POSITIVE_LEXICON.put("great", 0.75);
        POSITIVE_LEXICON.put("excellent", 0.9);
        POSITIVE_LEXICON.put("best", 0.95);
        POSITIVE_LEXICON.put("helpful", 0.75);
        POSITIVE_LEXICON.put("fantastic", 0.9);
        POSITIVE_LEXICON.put("super", 0.7);
        POSITIVE_LEXICON.put("good", 0.6);
        POSITIVE_LEXICON.put("thanks", 0.5);
        POSITIVE_LEXICON.put("thank", 0.5);
        POSITIVE_LEXICON.put("perfect", 0.95);
        POSITIVE_LEXICON.put("obsessed", 0.85);
        POSITIVE_LEXICON.put("wonderful", 0.85);
        POSITIVE_LEXICON.put("brilliant", 0.9);
        POSITIVE_LEXICON.put("quality", 0.65);
        POSITIVE_LEXICON.put("smooth", 0.7);
        POSITIVE_LEXICON.put("fast", 0.65);
        POSITIVE_LEXICON.put("incredible", 0.9);
        POSITIVE_LEXICON.put("gamechanger", 0.95);
        POSITIVE_LEXICON.put("works", 0.5);
        POSITIVE_LEXICON.put("fire", 0.9); // standalone slang "this is fire", "fire track"
        POSITIVE_LEXICON.put("lit", 0.85);
        POSITIVE_LEXICON.put("dope", 0.85);
        POSITIVE_LEXICON.put("banger", 0.9);
        POSITIVE_LEXICON.put("masterpiece", 0.95);
        POSITIVE_LEXICON.put("gem", 0.9);
        POSITIVE_LEXICON.put("goat", 0.95);
        POSITIVE_LEXICON.put("legend", 0.95);
        POSITIVE_LEXICON.put("underrated", 0.85);
        POSITIVE_LEXICON.put("goosebumps", 0.9);
        POSITIVE_LEXICON.put("fabulous", 0.9);
        POSITIVE_LEXICON.put("superb", 0.9);
        POSITIVE_LEXICON.put("outstanding", 0.9);
        POSITIVE_LEXICON.put("magical", 0.9);
        POSITIVE_LEXICON.put("soulful", 0.85);
        POSITIVE_LEXICON.put("nostalgic", 0.8);
        POSITIVE_LEXICON.put("master", 0.85);
        POSITIVE_LEXICON.put("king", 0.85);
        POSITIVE_LEXICON.put("queen", 0.85);
        POSITIVE_LEXICON.put("favourite", 0.9);
        POSITIVE_LEXICON.put("favorite", 0.9);
        POSITIVE_LEXICON.put("lovely", 0.85);
        POSITIVE_LEXICON.put("perfection", 0.95);
        POSITIVE_LEXICON.put("mindblowing", 0.95);
        POSITIVE_LEXICON.put("stellar", 0.9);
        POSITIVE_LEXICON.put("classic", 0.85);
        POSITIVE_LEXICON.put("epic", 0.9);
        POSITIVE_LEXICON.put("blessed", 0.85);
        POSITIVE_LEXICON.put("chills", 0.85);
        POSITIVE_LEXICON.put("wow", 0.8);
        POSITIVE_LEXICON.put("omg", 0.75);
        POSITIVE_LEXICON.put("vibes", 0.75);
        POSITIVE_LEXICON.put("cool", 0.7);
        POSITIVE_LEXICON.put("sweet", 0.7);
        POSITIVE_LEXICON.put("melody", 0.75);
        POSITIVE_LEXICON.put("voice", 0.7);
        POSITIVE_LEXICON.put("singing", 0.7);
        POSITIVE_LEXICON.put("lyrics", 0.7);
        POSITIVE_LEXICON.put("mesmerizing", 0.9);
        POSITIVE_LEXICON.put("evergreen", 0.9);
        POSITIVE_LEXICON.put("divine", 0.9);
        POSITIVE_LEXICON.put("soothing", 0.85);
        POSITIVE_LEXICON.put("informative", 0.8);
        POSITIVE_LEXICON.put("clarity", 0.8);
        POSITIVE_LEXICON.put("clear", 0.7);
        POSITIVE_LEXICON.put("simplified", 0.8);
        POSITIVE_LEXICON.put("valuable", 0.8);
        POSITIVE_LEXICON.put("insightful", 0.85);
        POSITIVE_LEXICON.put("insights", 0.8);
        POSITIVE_LEXICON.put("knowledgeable", 0.85);
        POSITIVE_LEXICON.put("bullish", 0.75);
        POSITIVE_LEXICON.put("profit", 0.8);
        POSITIVE_LEXICON.put("gain", 0.75);
        POSITIVE_LEXICON.put("gains", 0.75);
        POSITIVE_LEXICON.put("win", 0.8);
        POSITIVE_LEXICON.put("winning", 0.8);
        POSITIVE_LEXICON.put("respect", 0.85);
        POSITIVE_LEXICON.put("salute", 0.85);
        POSITIVE_LEXICON.put("proud", 0.85);
        POSITIVE_LEXICON.put("congrats", 0.85);
        POSITIVE_LEXICON.put("congratulations", 0.85);
        POSITIVE_LEXICON.put("clean", 0.75);
        POSITIVE_LEXICON.put("topnotch", 0.9);
        POSITIVE_LEXICON.put("accurate", 0.8);
        POSITIVE_LEXICON.put("authentic", 0.85);
        POSITIVE_LEXICON.put("genius", 0.9);
        POSITIVE_LEXICON.put("bravo", 0.85);
        POSITIVE_LEXICON.put("flawless", 0.95);
        POSITIVE_LEXICON.put("stunning", 0.9);
        POSITIVE_LEXICON.put("educational", 0.8);
        POSITIVE_LEXICON.put("beautiful", 0.9);

        // News & Current Events Positive Vocabulary
        POSITIVE_LEXICON.put("relief", 0.8);
        POSITIVE_LEXICON.put("recovery", 0.75);
        POSITIVE_LEXICON.put("progress", 0.8);
        POSITIVE_LEXICON.put("growth", 0.75);
        POSITIVE_LEXICON.put("record", 0.7);
        POSITIVE_LEXICON.put("victory", 0.9);
        POSITIVE_LEXICON.put("triumph", 0.9);
        POSITIVE_LEXICON.put("success", 0.85);
        POSITIVE_LEXICON.put("successful", 0.85);
        POSITIVE_LEXICON.put("safe", 0.75);
        POSITIVE_LEXICON.put("safety", 0.7);
        POSITIVE_LEXICON.put("rescue", 0.8);
        POSITIVE_LEXICON.put("saved", 0.8);
        POSITIVE_LEXICON.put("pleasant", 0.7);
        POSITIVE_LEXICON.put("sunny", 0.65);
        POSITIVE_LEXICON.put("improving", 0.7);
        POSITIVE_LEXICON.put("solution", 0.75);
        POSITIVE_LEXICON.put("win", 0.8);
        POSITIVE_LEXICON.put("gain", 0.7);
        POSITIVE_LEXICON.put("celebrate", 0.85);
        POSITIVE_LEXICON.put("celebration", 0.85);
        POSITIVE_LEXICON.put("peace", 0.9);
        POSITIVE_LEXICON.put("truth", 0.75);
        POSITIVE_LEXICON.put("informative", 0.75);
        POSITIVE_LEXICON.put("inspiring", 0.85);
    }

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;

    public CommentSentimentService(PostRepository postRepository,
                                   PostCommentRepository postCommentRepository) {
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
    }

    public static class TextAnalysisResult {
        public final String sentiment; // POSITIVE, NEUTRAL, NEGATIVE
        public final BigDecimal score; // -1.00 to +1.00
        public final String detectedKeyword;

        public TextAnalysisResult(String sentiment, BigDecimal score, String detectedKeyword) {
            this.sentiment = sentiment;
            this.score = score;
            this.detectedKeyword = detectedKeyword;
        }
    }

    /**
     * Java NLP text processor: evaluates comment sentiment using tokenization,
     * negation inversion, emoji detection, and domain-tuned lexicon scoring.
     */
    public static TextAnalysisResult analyzeText(String text) {
        if (text == null || text.isBlank()) {
            return new TextAnalysisResult("NEUTRAL", BigDecimal.ZERO, null);
        }

        double totalScore = 0.0;
        int matchedWords = 0;
        String topKeyword = null;

        // 1. Scan for high-impact emojis
        for (Map.Entry<String, Double> entry : EMOJI_LEXICON.entrySet()) {
            String emoji = entry.getKey();
            if (text.contains(emoji)) {
                int count = 0;
                int idx = 0;
                while ((idx = text.indexOf(emoji, idx)) != -1) {
                    count++;
                    idx += emoji.length();
                }
                int effectiveCount = Math.min(3, count);
                totalScore += entry.getValue() * effectiveCount;
                matchedWords += effectiveCount;
                if (topKeyword == null) {
                    topKeyword = emoji;
                }
            }
        }

        String lowerText = text.toLowerCase(Locale.ROOT);
        boolean fireIsNegative = lowerText.contains("caught fire") || lowerText.contains("wildfire")
                || lowerText.contains("fire hazard") || lowerText.contains("fire broke")
                || lowerText.contains("fire accident");

        // 2. Tokenize words for NLP analysis
        String[] tokens = WORD_SPLIT.split(lowerText);
        boolean negated = false;

        for (int i = 0; i < tokens.length; i++) {
            String word = tokens[i];
            if (word.isBlank()) continue;

            if (NEGATION_WORDS.contains(word)) {
                negated = true;
                continue;
            }

            if ("fire".equals(word) && fireIsNegative) {
                totalScore += -0.85;
                matchedWords++;
                if (topKeyword == null) topKeyword = "fire";
                continue;
            }

            if (NEGATIVE_LEXICON.containsKey(word)) {
                double val = NEGATIVE_LEXICON.get(word);
                if (negated) {
                    val = -val * 0.5; // "not broken" -> mild positive
                    negated = false;
                }
                totalScore += val;
                matchedWords++;
                if (topKeyword == null) topKeyword = word;
            } else if (POSITIVE_LEXICON.containsKey(word)) {
                double val = POSITIVE_LEXICON.get(word);
                if (negated) {
                    val = -val; // "not good" -> negative
                    negated = false;
                }
                totalScore += val;
                matchedWords++;
                if (topKeyword == null) topKeyword = word;
            } else {
                // Decay negation after 3 non-sentiment words
                if (i > 0 && i % 3 == 0) {
                    negated = false;
                }
            }
        }

        double normalizedScore = matchedWords > 0 
                ? Math.max(-1.0, Math.min(1.0, totalScore / Math.sqrt(matchedWords)))
                : 0.0;

        String sentiment;
        if (normalizedScore > 0.12) {
            sentiment = "POSITIVE";
        } else if (normalizedScore < -0.12) {
            sentiment = "NEGATIVE";
        } else {
            sentiment = "NEUTRAL";
        }

        BigDecimal score = BigDecimal.valueOf(normalizedScore).setScale(2, RoundingMode.HALF_UP);
        return new TextAnalysisResult(sentiment, score, topKeyword);
    }

    /**
     * Analyzes all comments for a post, calculates sentiment breakdown and Vanity Metrics alerts.
     */
    @Transactional
    public PostSentimentSummaryDto evaluatePostSentiment(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        List<PostComment> comments = postCommentRepository.findByPostIdOrderByCommentedAtDesc(postId);
        return updatePostSentimentAggregates(post, comments);
    }

    @Transactional
    public PostCommentDto addCommentAndAnalyze(Long postId, String author, String authorAvatar, String text) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        TextAnalysisResult result = analyzeText(text);

        PostComment comment = new PostComment(
                post,
                (author != null && !author.isBlank()) ? author.trim() : "Social User",
                (authorAvatar != null && !authorAvatar.isBlank()) ? authorAvatar : "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100",
                text.trim(),
                result.sentiment,
                result.score,
                result.detectedKeyword,
                LocalDateTime.now()
        );

        PostComment saved = postCommentRepository.save(comment);

        // Refresh post sentiment metrics
        List<PostComment> allComments = postCommentRepository.findByPostIdOrderByCommentedAtDesc(postId);
        updatePostSentimentAggregates(post, allComments);

        return toCommentDto(saved);
    }

    private PostSentimentSummaryDto updatePostSentimentAggregates(Post post, List<PostComment> comments) {
        int total = comments.size();
        int positive = 0;
        int neutral = 0;
        int negative = 0;
        double sumScore = 0.0;

        Map<String, Integer> issueCounts = new HashMap<>();
        Map<String, Integer> praiseCounts = new HashMap<>();

        for (PostComment c : comments) {
            sumScore += c.getSentimentScore().doubleValue();
            if ("POSITIVE".equalsIgnoreCase(c.getSentiment())) {
                positive++;
                if (c.getDetectedKeyword() != null) {
                    praiseCounts.merge(c.getDetectedKeyword(), 1, Integer::sum);
                }
            } else if ("NEGATIVE".equalsIgnoreCase(c.getSentiment())) {
                negative++;
                if (c.getDetectedKeyword() != null) {
                    issueCounts.merge(c.getDetectedKeyword(), 1, Integer::sum);
                }
            } else {
                neutral++;
            }
        }

        BigDecimal posPercent = total > 0 
                ? BigDecimal.valueOf((double) positive / total * 100).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal negPercent = total > 0 
                ? BigDecimal.valueOf((double) negative / total * 100).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal neuPercent = total > 0 
                ? BigDecimal.valueOf((double) neutral / total * 100).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal avgScore = total > 0 
                ? BigDecimal.valueOf(sumScore / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Vanity metrics detection logic: High likes but customer feedback is heavily negative
        boolean vanityAlert = (post.getLikesCount() >= 500 && negPercent.doubleValue() >= 40.0);
        String verdict;

        if (vanityAlert) {
            verdict = String.format("⚠️ Vanity Metrics Alert: Your engagement is up (%s likes), but %.0f%% of the feedback is negative. Raw likes hide customer complaints.",
                    String.format("%,d", post.getLikesCount()), negPercent.doubleValue());
        } else if (negPercent.doubleValue() >= 50.0) {
            verdict = String.format("Critical Sentiment Alert: %.0f%% of customer feedback is negative.", negPercent.doubleValue());
        } else if (posPercent.doubleValue() >= 60.0) {
            verdict = String.format("High Quality Engagement: %.0f%% of comments express positive sentiment and high brand affinity.", posPercent.doubleValue());
        } else {
            verdict = "Neutral engagement: Balanced mix of inquiries and feedback.";
        }

        post.setPositiveCommentsCount(positive);
        post.setNeutralCommentsCount(neutral);
        post.setNegativeCommentsCount(negative);
        post.setPositivePercent(posPercent);
        post.setNegativePercent(negPercent);
        post.setSentimentScore(avgScore);
        post.setVanityAlert(vanityAlert);
        post.setSentimentVerdict(verdict);
        postRepository.save(post);

        List<String> topIssues = issueCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(e -> e.getKey().toUpperCase() + " (" + e.getValue() + " mentions)")
                .toList();

        List<String> topPraises = praiseCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(e -> e.getKey().toUpperCase() + " (" + e.getValue() + " mentions)")
                .toList();

        List<PostCommentDto> commentDtos = comments.stream()
                .map(this::toCommentDto)
                .toList();

        PostSentimentSummaryDto summary = new PostSentimentSummaryDto();
        summary.setPostId(post.getId());
        summary.setLikesCount(post.getLikesCount());
        summary.setRealPostCommentsCount(post.getCommentsCount());
        summary.setPostCaption(post.getCaption());
        summary.setPlatform(post.getPlatform() != null ? post.getPlatform().name() : "");
        summary.setPostUrl(post.getPostUrl());
        summary.setTotalComments(total);
        summary.setPositiveCount(positive);
        summary.setNeutralCount(neutral);
        summary.setNegativeCount(negative);
        summary.setPositivePercent(posPercent);
        summary.setNeutralPercent(neuPercent);
        summary.setNegativePercent(negPercent);
        summary.setSentimentScore(avgScore);
        summary.setVanityAlert(vanityAlert);
        summary.setSentimentVerdict(verdict);
        summary.setTopIssues(topIssues);
        summary.setTopPraises(topPraises);
        summary.setComments(commentDtos);

        return summary;
    }

    private PostCommentDto toCommentDto(PostComment c) {
        PostCommentDto dto = new PostCommentDto();
        dto.setId(c.getId());
        dto.setAuthor(c.getAuthor());
        dto.setAuthorAvatar(c.getAuthorAvatar());
        dto.setCommentText(c.getCommentText());
        dto.setSentiment(c.getSentiment());
        dto.setSentimentScore(c.getSentimentScore());
        dto.setDetectedKeyword(c.getDetectedKeyword());
        dto.setCommentedAt(c.getCommentedAt());
        return dto;
    }
}
