package com.yoav_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.yoav_s.helper.AlertDialogHelper;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.model.Guide;
import com.yoav_s.model.GuideInteraction;
import com.yoav_s.model.GuideInteractions;
import com.yoav_s.model.User;
import com.yoav_s.model.Users;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.ADPTERS.GuideCommentsAdapter;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.GuideInteractionsViewModel;
import com.yoav_s.viewmodel.GuidesViewModel;
import com.yoav_s.viewmodel.UsersViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.widget.ScrollView;

public class GuideDetailsActivity extends BaseActivity {

    public static final String EXTRA_SELECTED_GUIDE_ID = "SELECTED_GUIDE_ID";

    private enum InteractionAction {
        NONE,
        LIKE,
        RATE,
        COMMENT,
        DELETE_COMMENT
    }

    private DrawerLayout drawerLayout;

    private ImageButton btnMenu;

    private TextView tvGuideTitle;
    private TextView tvAvgRating;
    private TextView tvLikesCount;
    private MaterialButton btnLikeGuide;

    private TextView tvGuideContent;

    private MaterialCardView cardRateSection;
    private RatingBar ratingBarGuide;
    private MaterialButton btnRateGuide;

    private RecyclerView rvGuideComments;

    private MaterialCardView cardCommentComposer;
    private EditText etComment;
    private MaterialButton btnSendComment;

    private MaterialButton btnBack;

    private ScrollView svGuideContent;

    private TextView navMyPlants;
    private TextView navCalendar;
    private TextView navSettings;
    private TextView navGuides;
    private TextView navAi;
    private TextView navLogout;

    private LauncherHelper launcherHelper;

    private GuidesViewModel guidesViewModel;
    private GuideInteractionsViewModel guideInteractionsViewModel;
    private UsersViewModel usersViewModel;

    private GuideCommentsAdapter commentsAdapter;

    private Guide selectedGuide;
    private String selectedGuideId;

    private final List<GuideInteraction> allGuideInteractions = new ArrayList<>();
    private final Map<String, String> userDisplayNameById = new HashMap<>();

    private GuideInteraction currentUserInteraction = null;
    private GuideInteraction pendingInteractionDraft = null;
    private boolean guideLoaded = false;
    private boolean interactionsLoaded = false;
    private boolean usersLoaded = false;
    private boolean currentUserInteractionResolved = false;

    private boolean actionInProgress = false;
    private InteractionAction pendingAction = InteractionAction.NONE;

    private boolean viewCountIncrementedThisOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setLayout(R.layout.activity_guide_details);
        setBottomNavigationVisibility(false);

        android.view.View contentFrame = findViewById(R.id.content_frame);
        DrawerLayout rootDrawer = contentFrame.findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(rootDrawer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeActivity();
    }

    @Override
    protected void initializeActivity() {
        launcherHelper = new LauncherHelper(this);
        initializeViews();
        configureGuideContentScrolling();
        configureRatingBar();
        readExtras();
        setRecyclerView();
        updateGuestUi();
        setListeners();
        setViewModel();
    }

    @Override
    protected void initializeViews() {
        android.view.View contentFrame = findViewById(R.id.content_frame);

        drawerLayout = contentFrame.findViewById(R.id.main);

        btnMenu = drawerLayout.findViewById(R.id.btnMenu);

        tvGuideTitle = drawerLayout.findViewById(R.id.tvGuideTitle);
        tvAvgRating = drawerLayout.findViewById(R.id.tvAvgRating);
        tvLikesCount = drawerLayout.findViewById(R.id.tvLikesCount);
        btnLikeGuide = drawerLayout.findViewById(R.id.btnLikeGuide);

        tvGuideContent = drawerLayout.findViewById(R.id.tvGuideContent);
        svGuideContent = drawerLayout.findViewById(R.id.svGuideContent);

        cardRateSection = drawerLayout.findViewById(R.id.cardRateSection);
        ratingBarGuide = drawerLayout.findViewById(R.id.ratingBarGuide);
        btnRateGuide = drawerLayout.findViewById(R.id.btnRateGuide);

        rvGuideComments = drawerLayout.findViewById(R.id.rvGuideComments);

        cardCommentComposer = drawerLayout.findViewById(R.id.cardCommentComposer);
        etComment = drawerLayout.findViewById(R.id.etComment);
        btnSendComment = drawerLayout.findViewById(R.id.btnSendComment);

        btnBack = drawerLayout.findViewById(R.id.btnBack);

        navMyPlants = drawerLayout.findViewById(R.id.navMyPlants);
        navCalendar = drawerLayout.findViewById(R.id.navCalendar);
        navSettings = drawerLayout.findViewById(R.id.navSettings);
        navGuides = drawerLayout.findViewById(R.id.navGuides);
        navAi = drawerLayout.findViewById(R.id.navAi);
        navLogout = drawerLayout.findViewById(R.id.navLogout);
    }

    private void configureGuideContentScrolling() {
        if (svGuideContent == null) return;

        svGuideContent.setVerticalScrollBarEnabled(true);

        svGuideContent.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);

            if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }

            return false;
        });
    }

    private void configureRatingBar() {
        ratingBarGuide.setNumStars(5);
        ratingBarGuide.setStepSize(0.5f);
        ratingBarGuide.setMax(10);
        ratingBarGuide.setIsIndicator(false);

        ratingBarGuide.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (!fromUser) return;

            float clamped = Math.max(0f, Math.min(5f, rating));
            float rounded = Math.round(clamped * 2f) / 2f;

            if (bar.getRating() != rounded) {
                bar.setRating(rounded);
            }
        });
    }

    private float clampRating(double rating) {
        if (rating < 0.0) return 0f;
        if (rating > 5.0) return 5f;
        return (float) rating;
    }

    private void readExtras() {
        Intent intent = getIntent();
        if (intent == null) {
            Toast.makeText(this, "Guide not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        selectedGuideId = intent.getStringExtra(EXTRA_SELECTED_GUIDE_ID);

        if (selectedGuideId == null || selectedGuideId.trim().isEmpty()) {
            Toast.makeText(this, "Guide not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setRecyclerView() {
        commentsAdapter = new GuideCommentsAdapter(new ArrayList<>());

        commentsAdapter.setListener(this::onCommentItemClicked);

        rvGuideComments.setLayoutManager(new LinearLayoutManager(this));
        rvGuideComments.setAdapter(commentsAdapter);
    }

    private void updateGuestUi() {
        boolean isGuest = currentUser == null;

        cardRateSection.setVisibility(isGuest ? android.view.View.GONE : android.view.View.VISIBLE);
        cardCommentComposer.setVisibility(isGuest ? android.view.View.GONE : android.view.View.VISIBLE);
    }

    @Override
    protected void setListeners() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        btnBack.setOnClickListener(v -> finish());

        btnLikeGuide.setOnClickListener(v -> onLikeClicked());

        btnRateGuide.setOnClickListener(v -> onRateClicked());

        btnSendComment.setOnClickListener(v -> onSendCommentClicked());

        navMyPlants.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            launcherHelper.launchActivity(MyPlantsActivity.class);
            finish();
        });

        navCalendar.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            launcherHelper.launchActivity(CalendarActivity.class);
            finish();
        });

        navSettings.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            launcherHelper.launchActivity(SettingsActivity.class);
            finish();
        });

        navGuides.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            launcherHelper.launchActivity(GuidesActivity.class);
            finish();
        });
        navAi.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            launcherHelper.launchActivity(AIAssistantActivity.class);
            finish();
        });

        navLogout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);

            currentUser = null;
            launcherHelper.launchActivity(SignInActivity.class);
            finish();
        });
    }

    @Override
    protected void setViewModel() {
        guidesViewModel = new ViewModelProvider(this).get(GuidesViewModel.class);
        guideInteractionsViewModel = new ViewModelProvider(this).get(GuideInteractionsViewModel.class);
        usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);

        showProgressDialog(null, "Loading guide...");

        // important: collection loaders first, then observe
        guideInteractionsViewModel.getByGuide(selectedGuideId);
        usersViewModel.getAll();
        guidesViewModel.get(selectedGuideId);

        if (currentUser != null && currentUser.getIdFs() != null) {
            guideInteractionsViewModel.getByGuideAndUser(selectedGuideId, currentUser.getIdFs());
        } else {
            currentUserInteractionResolved = true;
        }

        guidesViewModel.getLiveDataEntity().observe(this, this::handleGuideChanged);
        guideInteractionsViewModel.getLiveDataCollection().observe(this, this::handleGuideInteractionsChanged);
        usersViewModel.getLiveDataCollection().observe(this, this::handleUsersChanged);

        if (currentUser != null) {
            guideInteractionsViewModel.getLiveDataEntity().observe(this, interaction -> {
                currentUserInteraction = interaction;
                currentUserInteractionResolved = true;
                updateInteractionControls();
                hideLoadingIfReady();
            });
        }

        guideInteractionsViewModel.getSuccess().observe(this, success -> {
            if (!actionInProgress) return;

            hideProgressDialog();

            if (!Boolean.TRUE.equals(success)) {
                actionInProgress = false;
                pendingAction = InteractionAction.NONE;
                pendingInteractionDraft = null;
                Toast.makeText(this, "Could not save interaction", Toast.LENGTH_SHORT).show();
                return;
            }

            InteractionAction completedAction = pendingAction;

            if (pendingInteractionDraft != null) {
                currentUserInteraction = pendingInteractionDraft;
            }

            actionInProgress = false;
            pendingAction = InteractionAction.NONE;
            pendingInteractionDraft = null;

            if (completedAction == InteractionAction.LIKE) {
                Toast.makeText(this, "Like updated", Toast.LENGTH_SHORT).show();
            } else if (completedAction == InteractionAction.RATE) {
                Toast.makeText(this, "Rating updated", Toast.LENGTH_SHORT).show();
            } else if (completedAction == InteractionAction.COMMENT) {
                Toast.makeText(this, "Comment saved", Toast.LENGTH_SHORT).show();
                etComment.setText("");
            } else if (completedAction == InteractionAction.DELETE_COMMENT) {
                Toast.makeText(this, "Comment deleted", Toast.LENGTH_SHORT).show();
                etComment.setText("");
            }

            updateInteractionControls();
        });
    }

    private void handleGuideChanged(Guide guide) {
        guideLoaded = true;

        if (guide == null) {
            Toast.makeText(this, "Guide not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        selectedGuide = guide;

        tvGuideTitle.setText(safeText(guide.getTitle()));
        tvGuideContent.setText(safeText(guide.getText()));

        incrementGuideViewsOnce();

        hideLoadingIfReady();
    }

    private void incrementGuideViewsOnce() {
        if (selectedGuide == null) return;
        if (selectedGuide.getIdFs() == null || selectedGuide.getIdFs().trim().isEmpty()) return;
        if (viewCountIncrementedThisOpen) return;

        viewCountIncrementedThisOpen = true;

        int currentViews = Math.max(selectedGuide.getViewsCount(), 0);
        selectedGuide.setViewsCount(currentViews + 1);

        guidesViewModel.update(selectedGuide);
    }

    private void handleGuideInteractionsChanged(GuideInteractions interactions) {
        interactionsLoaded = true;
        allGuideInteractions.clear();

        if (interactions != null) {
            allGuideInteractions.addAll(interactions);
        }

        updateGuideStatsAndComments();
        hideLoadingIfReady();
    }

    private void handleUsersChanged(Users users) {
        usersLoaded = true;
        userDisplayNameById.clear();

        if (users != null) {
            for (User user : users) {
                if (user == null || user.getIdFs() == null) continue;
                userDisplayNameById.put(user.getIdFs(), safeText(user.getDisplayName()));
            }
        }

        commentsAdapter.setUserDisplayNameById(new HashMap<>(userDisplayNameById));
        hideLoadingIfReady();
    }

    private void updateGuideStatsAndComments() {
        int likesCount = 0;
        int ratingsCount = 0;
        double ratingsSum = 0.0;

        List<GuideInteraction> comments = new ArrayList<>();

        for (GuideInteraction interaction : allGuideInteractions) {
            if (interaction == null) continue;

            if (interaction.isLike()) {
                likesCount++;
            }

            if (interaction.getRating() > 0) {
                ratingsSum += interaction.getRating();
                ratingsCount++;
            }

            if (interaction.getBody() != null && !interaction.getBody().trim().isEmpty()) {
                comments.add(interaction);
            }
        }

        Collections.sort(comments, new Comparator<GuideInteraction>() {
            @Override
            public int compare(GuideInteraction i1, GuideInteraction i2) {
                long t1 = i1 != null && i1.getCreatedAt() != null ? i1.getCreatedAt().toDate().getTime() : 0L;
                long t2 = i2 != null && i2.getCreatedAt() != null ? i2.getCreatedAt().toDate().getTime() : 0L;
                return Long.compare(t2, t1);
            }
        });

        double avgRating = ratingsCount > 0 ? (ratingsSum / ratingsCount) : 0.0;

        tvAvgRating.setText(String.format(Locale.getDefault(), "%.1f★", avgRating));
        tvLikesCount.setText(String.valueOf(likesCount));

        commentsAdapter.setUserDisplayNameById(new HashMap<>(userDisplayNameById));
        commentsAdapter.setItems(comments);

        updateInteractionControls();
    }

    private void updateInteractionControls() {
        boolean guest = currentUser == null;

        if (guest) {
            btnLikeGuide.setText("Like");
            ratingBarGuide.setRating(0f);
            return;
        }

        boolean liked = currentUserInteraction != null && currentUserInteraction.isLike();
        btnLikeGuide.setText(liked ? "Liked" : "Like");

        double existingRating = currentUserInteraction != null ? currentUserInteraction.getRating() : 0.0;
        ratingBarGuide.setIsIndicator(false);
        ratingBarGuide.setRating(clampRating(existingRating));
        btnRateGuide.setEnabled(true);
        btnRateGuide.setAlpha(1f);
        btnRateGuide.setText(existingRating > 0 ? "Update rating★" : "Rate★");

        String existingComment = currentUserInteraction != null && currentUserInteraction.getBody() != null
                ? currentUserInteraction.getBody().trim()
                : "";

        if (existingComment.isEmpty()) {
            etComment.setHint("Add a comment");
        } else {
            etComment.setHint("Update your comment");
        }
    }

    private void onLikeClicked() {
        if (currentUser == null) {
            Toast.makeText(this, "You must sign in/register to interact with the guide", Toast.LENGTH_SHORT).show();
            return;
        }

        GuideInteraction draft = buildInteractionDraft();
        if (draft == null) {
            Toast.makeText(this, "Could not update like", Toast.LENGTH_SHORT).show();
            return;
        }

        draft.setLike(!draft.isLike());

        saveInteraction(draft, InteractionAction.LIKE, "Saving like...");
    }

    private void onRateClicked() {
        if (currentUser == null) {
            Toast.makeText(this, "You must sign in/register to interact with the guide", Toast.LENGTH_SHORT).show();
            return;
        }

        float selectedRating = clampRating(ratingBarGuide.getRating());
        if (selectedRating <= 0f) {
            Toast.makeText(this, "Choose a rating first", Toast.LENGTH_SHORT).show();
            return;
        }

        GuideInteraction draft = buildInteractionDraft();
        if (draft == null) {
            Toast.makeText(this, "Could not publish rating", Toast.LENGTH_SHORT).show();
            return;
        }

        draft.setRating(selectedRating);

        saveInteraction(draft, InteractionAction.RATE, "Publishing rating...");
    }

    private void onSendCommentClicked() {
        if (currentUser == null) {
            Toast.makeText(this, "You must sign in/register to interact with the guide", Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = etComment.getText() != null ? etComment.getText().toString().trim() : "";
        if (comment.isEmpty()) {
            Toast.makeText(this, "Write a comment first", Toast.LENGTH_SHORT).show();
            return;
        }

        GuideInteraction draft = buildInteractionDraft();
        if (draft == null) {
            Toast.makeText(this, "Could not send comment", Toast.LENGTH_SHORT).show();
            return;
        }

        draft.setBody(comment);
        draft.setCreatedAt(Timestamp.now());

        saveInteraction(draft, InteractionAction.COMMENT, "Sending comment...");
    }

    private GuideInteraction buildInteractionDraft() {
        if (selectedGuide == null || currentUser == null || currentUser.getIdFs() == null) {
            return null;
        }

        GuideInteraction draft = new GuideInteraction();

        if (currentUserInteraction != null) {
            draft.setIdFs(currentUserInteraction.getIdFs());
            draft.setGuideId(currentUserInteraction.getGuideId());
            draft.setUserId(currentUserInteraction.getUserId());
            draft.setBody(currentUserInteraction.getBody());
            draft.setCreatedAt(currentUserInteraction.getCreatedAt());
            draft.setLike(currentUserInteraction.isLike());
            draft.setRating(currentUserInteraction.getRating());
        } else {
            draft.setGuideId(selectedGuide.getIdFs());
            draft.setUserId(currentUser.getIdFs());
            draft.setBody("");
            draft.setCreatedAt(null);
            draft.setLike(false);
            draft.setRating(0.0);
        }

        return draft;
    }

    private void saveInteraction(GuideInteraction interaction, InteractionAction action, String progressMessage) {
        actionInProgress = true;
        pendingAction = action;
        pendingInteractionDraft = interaction;

        showProgressDialog(null, progressMessage);

        if (interaction.getIdFs() == null || interaction.getIdFs().trim().isEmpty()) {
            guideInteractionsViewModel.add(interaction);
        } else {
            guideInteractionsViewModel.update(interaction);
        }
    }

    private void onCommentItemClicked(GuideInteraction interaction) {
        if (interaction == null || currentUser == null || currentUser.getIdFs() == null) {
            return;
        }

        if (interaction.getBody() == null || interaction.getBody().trim().isEmpty()) {
            return;
        }

        if (!currentUser.getIdFs().equals(interaction.getUserId())) {
            return;
        }

        AlertDialogHelper.showDelete(
                this,
                "Do you want to delete your comment?",
                () -> deleteCurrentUserComment(interaction),
                () -> {
                    // pressed no
                }
        );
    }

    private void deleteCurrentUserComment(GuideInteraction interaction) {
        GuideInteraction draft = buildInteractionDraft();
        if (draft == null) {
            Toast.makeText(this, "Could not delete comment", Toast.LENGTH_SHORT).show();
            return;
        }

        draft.setBody("");
        draft.setCreatedAt(null);

        saveInteraction(draft, InteractionAction.DELETE_COMMENT, "Deleting comment...");
    }

    private void hideLoadingIfReady() {
        boolean authResolved = currentUser == null || currentUserInteractionResolved;

        if (guideLoaded && interactionsLoaded && usersLoaded && authResolved && !actionInProgress) {
            hideProgressDialog();
        }
    }

    private static String safeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "-";
        }
        return text.trim();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
            return;
        }
        super.onBackPressed();
    }
}