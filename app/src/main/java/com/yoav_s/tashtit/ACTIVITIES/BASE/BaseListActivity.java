package com.yoav_s.tashtit.ACTIVITIES.BASE;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yoav_s.helper.AlertDialogHelper;
import com.yoav_s.helper.BitMapHelper;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.helper.ListUtil;
import com.yoav_s.helper.StringUtil;
import com.yoav_s.helper.inputValidators.EntryValidation;
import com.yoav_s.helper.inputValidators.Validator;
import com.yoav_s.model.BASE.BaseEntity;
import com.yoav_s.model.BASE.BaseList;
import com.yoav_s.model.BASE.IHasNameAndPicture;
import com.uri_r.tashtit.ADPTERS.BASE.GenericAdapter;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.BASE.BaseViewModel;

import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.annotation.DrawableRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * GENERIC BASE ACTIVITY FOR LIST MANAGEMENT:
 * This class abstracts away all common list operations (Add, Edit, Delete UI, ViewModel binding).
 *
 * @param <T>  The data model type (e.g., ToyCategory). Must extend BaseEntity (for ID) and IHasNameAndPicture (for name/picture).
 * @param <L>  The Collection Type (e.g., CategoryList). Required to satisfy the two-argument BaseViewModel constraint.
 * @param <VM> The ViewModel type (e.g., CategoryViewModel). Must extend BaseViewModel<T, L>.
 */
public abstract class BaseListActivity<T extends BaseEntity & IHasNameAndPicture, L extends BaseList<T, L>, VM extends BaseViewModel<T, L>> extends BaseActivity implements EntryValidation {

    //region CONFIGURATION

    // ==================== CONFIGURATION CLASS ====================
    /**
     * Configuration class that holds all customization settings for the activity.
     */
    public static class ActivityConfig {
        // Header settings
        public String headerTitle;
        public int headerTitleColor = 0;

        // Background settings
        public int backgroundColor = 0;
        public int backgroundDrawable = 0;

        // Item settings
        public int defaultItemPicture = R.drawable.infrastructure;
        public int itemLayoutId = R.layout.single_base_layout;

        // Button icons
        public int okButtonIcon = R.drawable.ok;
        public int cancelButtonIcon = R.drawable.not_ok;

        // Feature flags
        public boolean showPictureInput = false;

        // Request validation
        public boolean validateInput = true;


        /**
         * Builder Pattern for clean and readable configuration.
         */
        public static class Builder {
            private ActivityConfig config = new ActivityConfig();

            public Builder headerTitle(String title) {
                config.headerTitle = title;
                return this;
            }

            public Builder headerTitleColor(int color) {
                config.headerTitleColor = color;
                return this;
            }

            public Builder backgroundColor(int color) {
                config.backgroundColor = color;
                return this;
            }

            public Builder backgroundDrawable(@DrawableRes int drawable) {
                config.backgroundDrawable = drawable;
                return this;
            }

            public Builder defaultItemPicture(@DrawableRes int drawable) {
                config.defaultItemPicture = drawable;
                return this;
            }

            public Builder itemLayoutId(int layoutId) {
                config.itemLayoutId = layoutId;
                return this;
            }

            public Builder okButtonIcon(@DrawableRes int icon) {
                config.okButtonIcon = icon;
                return this;
            }

            public Builder cancelButtonIcon(@DrawableRes int icon) {
                config.cancelButtonIcon = icon;
                return this;
            }

            public Builder showPictureInput(boolean show) {
                config.showPictureInput = show;
                return this;
            }

            public Builder validateInput(boolean validate) {
                config.validateInput = validate;
                return this;
            }

            public ActivityConfig build() {
                // Validation: itemLayoutId is mandatory
                if (config.itemLayoutId == 0) {
                    throw new IllegalStateException("itemLayoutId must be set in ActivityConfig");
                }
                return config;
            }
        }
    }

    //endregion CONFIGURATION

    //region UI COMPONENTS

    // ==================== UI COMPONENTS ====================

    protected TextView              tvHeaderTitle;
    protected ConstraintLayout      clEdit;
    protected ImageView             ivName;
    protected EditText              etName;
    protected ImageView             btnOk;
    protected ImageView             btnCancel;
    protected RecyclerView          rvList;
    protected FloatingActionButton  btnAdd;
    protected ConstraintLayout      mainView;

    //endregion UI COMPONENTS

    //region DATA MANAGEMENT

    // ==================== DATA MANAGEMENT ====================

    protected VM                viewModel;
    protected GenericAdapter<T> adapter;
    protected T                 oldItem;
    protected Bitmap            image;
    protected LauncherHelper    launcher;

    // Configuration object - THIS IS THE KEY!
    protected ActivityConfig config;

    //endregion DATA MANAGEMENT

    //region ABSTRACT METHODS

    // ==================== ABSTRACT METHODS (Only 5!) ====================

    /**
     * 1. Configuration: Returns the ActivityConfig object with all settings.
     */
    protected abstract ActivityConfig getConfig();

    /**
     * 2. ViewModel Class: Returns the concrete ViewModel class used for this activity.
     */
    protected abstract Class<VM> getViewModelClass();

    /**
     * 3. Object Creation: Called when saving. Must create the concrete object (T) using the name input.
     */
    protected abstract T createNewItem(String name);

    /**
     * 4. Validation Rules: Sets up the specific validation rules for the input fields.
     */
    protected abstract void setupValidation();

    /**
     * 5. Adapter Hooks: Place to define custom click, long click (delete), and view binding logic.
     */
    protected abstract void setupAdapterHooks(GenericAdapter<T> adapter);

    //endregion ABSTRACT METHODS

    //region ACTIVITY LIFECYCLE

    // ==================== ACTIVITY LIFECYCLE ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_base_list);
        setLayout(R.layout.activity_base_list);

        // CRITICAL: Get configuration FIRST (after super.onCreate so getResources() works)
        config = getConfig();

        EdgeToEdge.enable(this);
        mainView = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //setupBottomNavigation();

        initializeViews();
        applyCustomization();
        setViewModel();
        setRecyclerView();

        if (config.showPictureInput) {
            launcher = new LauncherHelper(this);
        }
    }

    //endregion ACTIVITY LIFECYCLE

    //region CUSTOMIZATION LOGIC

    // ==================== CUSTOMIZATION LOGIC ====================

    /**
     * Applies all customization settings from the config object.
     */
    protected void applyCustomization() {
        // 1. Background (Drawable has priority over color)
        if (config.backgroundDrawable != 0) {
            mainView.setBackgroundResource(config.backgroundDrawable);
        } else if (config.backgroundColor != 0) {
            mainView.setBackgroundColor(config.backgroundColor);
        }

        // 2. Header Title
        if (config.headerTitle != null) {
            tvHeaderTitle.setText(config.headerTitle);
        }

        // 3. Header Title Color
        if (config.headerTitleColor != 0) {
            tvHeaderTitle.setTextColor(config.headerTitleColor);
        }

        // 4. OK Button Icon
        if (config.okButtonIcon != 0) {
            btnOk.setImageResource(config.okButtonIcon);
        }

        // 5. Cancel Button Icon
        if (config.cancelButtonIcon != 0) {
            btnCancel.setImageResource(config.cancelButtonIcon);
        }
    }

    //endregion CUSTOMIZATION LOGIC

    //region UI INITIALIZATION

    // ==================== UI INITIALIZATION ====================

    @Override
    protected void initializeViews() {
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        clEdit        = findViewById(R.id.clEdit);
        clEdit.setVisibility(View.GONE);

        ivName    = findViewById(R.id.ivName);
        etName    = findViewById(R.id.etName);
        btnOk     = findViewById(R.id.btnOk);
        btnCancel = findViewById(R.id.btnCancel);
        rvList    = findViewById(R.id.rvList);
        btnAdd    = findViewById(R.id.fabAdd);


        // Set picture input visibility based on config
        if (config.showPictureInput) {
            ivName.setVisibility(View.VISIBLE);
        } else {
            ivName.setVisibility(View.GONE);
        }

        setListeners();
    }

    @Override
    protected void setListeners() {
        // 1. Add New Item Button
        btnAdd.setOnClickListener(v -> {
            oldItem = null;
            image   = null;
            etName.setText("");
            if (config.showPictureInput) {
                ivName.setImageResource(config.defaultItemPicture);
            }
            showEditPanel();
        });

        // 2. OK Button (Save/Update)
        btnOk.setOnClickListener(v -> {
            if (validateInput()) {
                String inputName = etName.getText().toString().trim();

                if (!ListUtil.containsItem(adapter.getItems(), inputName, IHasNameAndPicture::getName)) {
                    if (oldItem != null) {
                        updateExistingItem(oldItem, inputName);
                    } else {
                        addNewItem(inputName);
                    }
                }
                else {
                    Toast.makeText(this, inputName + " already exists", Toast.LENGTH_SHORT).show();
                }

                resetEditMode();
            }
        });

        // 3. Cancel Button
        btnCancel.setOnClickListener(v -> {
            resetEditMode();
        });

        // 4. Picture Input (only if enabled)
        if (config.showPictureInput) {
            ivName.setOnClickListener(v -> {
                AlertDialogHelper.showTakePicture(
                        this,
                        () -> {
                            launcher.takePhotoWithPermissionCheck(bitmap -> {
                                if (bitmap != null) {
                                    image = bitmap;
                                    ivName.setImageBitmap(bitmap);
                                } else {
                                    showImageError();
                                }
                            });
                        },
                        () -> {
                            launcher.pickImage(uri -> {
                                if (uri != null) {
                                    image = BitMapHelper.getBitmapFromUri(
                                            BaseListActivity.this,
                                            uri,
                                            ivName.getWidth(),
                                            ivName.getHeight()
                                    );
                                    if (image != null) {
                                        ivName.setImageBitmap(image);
                                    } else {
                                        showImageError();
                                    }
                                } else {
                                    showImageError();
                                }
                            });
                        }
                );
            });
        }
    }

    //endregion UI INITIALIZATION

    //region HELPER METHODS

    // ==================== HELPER METHODS ====================

    /**
     * Updates an existing item in the list.
     */
    private void updateExistingItem(T item, String newName) {
        item.setName(newName);
        if (image != null) {
            item.setPicture(BitMapHelper.encodeTobase64(image));
        }
        adapter.notifyItemChanged(adapter.getItems().indexOf(item));
        viewModel.save(item);
    }

    /**
     * Adds a new item to the list.
     */
    private void addNewItem(String name) {
        T newItem = createNewItem(name);
        if (image != null) {
            newItem.setPicture(BitMapHelper.encodeTobase64(image));
        }
        viewModel.save(newItem);
    }

    /**
     * Resets the edit panel to its initial state.
     */
    private void resetEditMode() {
        oldItem = null;
        image   = null;
        etName.setText("");
        if (config.showPictureInput) {
            ivName.setImageResource(config.defaultItemPicture);
        }
        hideEditPanel();
    }

    /**
     * Shows the edit panel with animation.
     */
    private void showEditPanel() {
        clEdit.setVisibility(View.VISIBLE);
        clEdit.setAlpha(0f);
        clEdit.animate()
                .alpha(1f)
                .setDuration(200)
                .start();
    }

    /**
     * Hides the edit panel with animation.
     */
    private void hideEditPanel() {
        clEdit.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> clEdit.setVisibility(View.GONE))
                .start();
    }

    /**
     * Shows error message for image operations.
     */
    private void showImageError() {
        Toast.makeText(this, "Photo failed or permission denied", Toast.LENGTH_SHORT).show();
    }

    /**
     * Allows editing an existing item (can be called from setupAdapterHooks).
     */
    protected void editItem(T item) {
        oldItem = item;
        etName.setText(item.getName());

        if (config.showPictureInput && !StringUtil.isNullOrEmpty(item.getPicture())) {
            image = BitMapHelper.decodeBase64(item.getPicture());
            ivName.setImageBitmap(image);
        }

        showEditPanel();
    }

    /**
     * Executes the validation setup defined by the concrete class.
     */
    protected boolean validateInput() {
        if (config.validateInput) {
            Validator.clear();
            setupValidation();
            return Validator.validate();
        }
        return true;
    }

    //endregion HELPER METHODS

    //region DATA BINDING

    // ==================== DATA BINDING ====================

    @Override
    protected void setViewModel() {
        viewModel = new ViewModelProvider(this).get(getViewModelClass());

        showProgressDialog(config.headerTitle, "Loading...");
        viewModel.getAll();

        viewModel.getLiveDataCollection().observe(this, new Observer<L>() {
            @Override
            @SuppressWarnings("unchecked")
            public void onChanged(L collection) {
                if (collection != null && adapter != null) {
                    adapter.setItems((List<T>) collection);
                }
                hideProgressDialog();
            }
        });
    }

    protected void setRecyclerView() {
        adapter = new GenericAdapter<>(
                null,
                config.itemLayoutId,
                // 1. View Holder Setup
                holder -> {
                    holder.putView("tvValue", holder.itemView.findViewById(R.id.tvValue));
                    View pictureView = holder.itemView.findViewById(R.id.ivPicture);
                    if (pictureView != null) {
                        holder.putView("ivPicture", pictureView);
                    }
                },
                // 2. View Binding - FIXED: Use config.defaultItemPicture
                (holder, item, position) -> {
                    ((TextView) holder.getView("tvValue")).setText(item.getName());

                    ImageView ivPicture = holder.getView("ivPicture");
                    if (ivPicture != null) {
                        if (!StringUtil.isNullOrEmpty(item.getPicture())) {
                            ivPicture.setImageBitmap(BitMapHelper.decodeBase64(item.getPicture()));
                        } else {
                            // CRITICAL FIX: Use config.defaultItemPicture instead of hardcoded value
                            ivPicture.setImageResource(config.defaultItemPicture);
                        }
                    }
                }
        );

        rvList.setAdapter(adapter);
        rvList.setLayoutManager(new LinearLayoutManager(this));

        setupAdapterHooks(adapter);
    }

    //endregion DATA BINDING

    //region CLEANUP

    // ==================== CLEANUP ====================

    @Override
    protected void onStop() {
        if (viewModel != null) {
            viewModel.stopListening();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (viewModel != null) {
            viewModel.stopListening();
        }
        super.onDestroy();
    }

    //endregion CLEANUP
}