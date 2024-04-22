import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.immocalc.myapplication.R;

public class ExpandableLayout extends FrameLayout {
    private Context mContext;
    private AttributeSet mAttrs;
    private NestedScrollView nestedScrollView;
    private LinearLayout verticalLayout;
    private LinearLayout horizontalLayout;
    private TextView titleView;
    private FrameLayout contentFrameLayout;
    private ImageView arrowView;
    private int uniqueId;
    private boolean expanded = false;
    private boolean animating = false;
    private long animDuration = 500L;
    private String titleText;
    private Drawable arrowImage;
    private ValueAnimator expandAnimator = ValueAnimator.ofFloat(0f, 1f);

    public ExpandableLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mAttrs = attrs;

        init(context, attrs);

        expandAnimator.setDuration(animDuration);
        expandAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            arrowView.setRotation(progress * 180);
        });
        expandAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                animating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animating = false;
            }
        });
        // Initialisation de l'état initial du layout lorsque la vue est prête
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Supprimer le listener pour éviter les appels répétés
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }
    @SuppressLint("SetTextI18n")
    private void init(Context context, AttributeSet attrs) {
        // Récupération des attributs dans attrs
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableLayout);
        arrowImage = a.getDrawable(R.styleable.ExpandableLayout_arrowImage);
        titleText = a.getString(R.styleable.ExpandableLayout_titleText);
        expanded = a.getBoolean(R.styleable.ExpandableLayout_startExpanded, expanded);
        a.recycle();

        nestedScrollView = new NestedScrollView(context);
        nestedScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Création du LinearLayout vertical
        verticalLayout = new LinearLayout(context);
        verticalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        verticalLayout.setOrientation(LinearLayout.VERTICAL);

        nestedScrollView.addView(verticalLayout);

        // Création du LinearLayout horizontal
        horizontalLayout = new LinearLayout(context);
        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setPaddingRelative(30, 0, 30, 0);

        // Création du TextView
        titleView = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0, // Utiliser 0 pour occuper l'espace restant
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1 // Poids 1 pour que le TextView utilise tout l'espace disponible
        );
        layoutParams.gravity = Gravity.CENTER_VERTICAL; // Centrer verticalement le texte
        titleView.setLayoutParams(layoutParams);
        if (titleText == null) {
            titleText = "Titre";
        }
        titleView.setText(titleText);

        // Augmenter la taille du texte et le mettre en gras
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Taille du texte en sp
        titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);

        // Création de l'ImageView
        arrowView = new ImageView(context);
        arrowView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        if (arrowImage == null) {
            Drawable drawable = arrowImage;
            arrowView.setImageDrawable(drawable);
        } else {
            arrowView.setImageDrawable(arrowImage);
        }
        // Ajout du TextView et de l'ImageView au LinearLayout horizontal
        horizontalLayout.addView(titleView);
        horizontalLayout.addView(arrowView);

        // Ajout du LinearLayout horizontal au LinearLayout vertical
        verticalLayout.addView(horizontalLayout);

        // Ajout du FrameLayout au LinearLayout vertical
        contentFrameLayout = new FrameLayout(context);
        contentFrameLayout.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT,// LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));
        contentFrameLayout.setPadding(30, 30, 30, 30);
        verticalLayout.addView(contentFrameLayout);

        // Ajout du LinearLayout vertical à l'ExpandableLayout
        //addView(verticalLayout);
        addView(nestedScrollView);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (titleView == null && contentFrameLayout == null && arrowView == null) {
            init(mContext, mAttrs);
        }

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            // Vérifier si la vue n'est pas déjà ajoutée par ExpandableLayout
            if (child != nestedScrollView && child != contentFrameLayout) {
                // Retirer l'enfant de son parent d'origine s'il en a un
                if (child.getParent() != null) {
                    ((ViewGroup) child.getParent()).removeView(child);
                }
                // Ajouter chaque enfant à contentFrameLayout
                contentFrameLayout.addView(child);
            }
        }
        if (titleView != null && contentFrameLayout != null && arrowView != null) {
            if (expanded) {
                arrowView.setRotation(180f);
            } else {
                contentFrameLayout.getLayoutParams().height = 0;
                contentFrameLayout.requestLayout();
                arrowView.setRotation(0f);
            }
            // Ajouter le OnClickListener à la fois à titleView et arrowView
            OnClickListener clickListener = v -> {
                if (animating) {
                    return; // Ne rien faire si une animation est déjà en cours
                }

                if (expanded) {
                    collapseContent();
                    arrowView.setImageDrawable(arrowImage);
                } else {
                    expandContent();
                    arrowView.setImageDrawable(arrowImage);
                }
                expanded = !expanded;
                arrowView.setRotation(expanded ? 180f : 0f);
            };
            titleView.setOnClickListener(clickListener);
            arrowView.setOnClickListener(clickListener);

        } else {
            Log.e("ExpandableLayout", "Impossible de trouver les vues avec les IDs spécifiés.");
        }
        //}
    }
    private void expandContent() {
        ViewParent parent = null;
        if (contentFrameLayout != null && arrowView != null) {
            int contentHeight = getContentHeight();
            parent = null;
            int parentHeight = 0;
          //  boolean updateParents = true;

          //  if (updateParents) {
                parent = getParent();
                while (parent != null && !(parent instanceof ExpandableLayout)) {
                    parent = parent.getParent();
                }

                if (parent != null) {
                    parentHeight = ((ExpandableLayout) parent).contentFrameLayout.getHeight();
                    ViewGroup.LayoutParams params = ((ExpandableLayout) parent).contentFrameLayout.getLayoutParams();
                    params.height = parentHeight+contentHeight; // Nouvelle hauteur en pixels
                    ((ExpandableLayout) parent).contentFrameLayout.setLayoutParams(params);
                 //   Log.e("EXPANDABLE LAYOUT", "Parent trouvé hauteur =" + parentHeight + "    Hauteur enfant = " + contentHeight);
                }

            ValueAnimator animator = ValueAnimator.ofInt(0, contentHeight);
            animator.addUpdateListener(animation -> {
                int value = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = contentFrameLayout.getLayoutParams();
                layoutParams.height = value;
                contentFrameLayout.setLayoutParams(layoutParams);
            });
            animator.setDuration(animDuration);
            animator.start();

            ObjectAnimator arrowAnimator = ObjectAnimator.ofFloat(arrowView, "rotation", 0f, 180f);
            arrowAnimator.setDuration(animDuration);
            arrowAnimator.start();
        } else {
           // Log.e("ExpandableLayout", "contentLayout ou arrowView est null dans expandContent()");
        }
    }
    private void collapseContent() {
        if (contentFrameLayout != null && arrowView != null) {
            int contentHeight = getContentHeight();
            ViewParent parent = null;
            int parentHeight = 0;
            parent = getParent();
            while (parent != null && !(parent instanceof ExpandableLayout)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                parentHeight = ((ExpandableLayout) parent).contentFrameLayout.getHeight();
                ViewGroup.LayoutParams params = ((ExpandableLayout) parent).contentFrameLayout.getLayoutParams();
                params.height = parentHeight - contentHeight; // Nouvelle hauteur en pixels

                ValueAnimator parentAnimator = ValueAnimator.ofInt(parentHeight, parentHeight - contentHeight);
                ViewParent finalParent = parent;
                parentAnimator.addUpdateListener(parentAnimation -> {
                    int value = (int) parentAnimation.getAnimatedValue();
                    ViewGroup.LayoutParams parentLayoutParams = ((ExpandableLayout) finalParent).contentFrameLayout.getLayoutParams();
                    parentLayoutParams.height = value;
                    ((ExpandableLayout) finalParent).contentFrameLayout.setLayoutParams(parentLayoutParams);
                });
                parentAnimator.setDuration(animDuration);
                parentAnimator.start();
            }

            ValueAnimator contentAnimator = ValueAnimator.ofInt(contentHeight, 0);
            contentAnimator.addUpdateListener(animation -> {
                int value = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = contentFrameLayout.getLayoutParams();
                layoutParams.height = value;
                contentFrameLayout.setLayoutParams(layoutParams);
            });
            contentAnimator.setDuration(animDuration);
            contentAnimator.start();

            ObjectAnimator arrowAnimator = ObjectAnimator.ofFloat(arrowView, "rotation", 180f, 0f);
            arrowAnimator.setDuration(animDuration);
            arrowAnimator.start();
        } else {
            Log.e("ExpandableLayout", "contentLayout ou arrowView est null dans collapseContent()");
        }
    }

    private int getContentHeight() {
        // Mesurer la hauteur réelle du contenu
        contentFrameLayout.measure(
                MeasureSpec.makeMeasureSpec(contentFrameLayout.getWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );
        return contentFrameLayout.getMeasuredHeight();
    }
    public void setTitle(String title) {
        titleText = title;
    }
    public void setImage(Drawable drawable) {
        arrowImage = drawable;
    }
    public void setImage(int imageResId) { arrowImage = ResourcesCompat.getDrawable(getResources(), imageResId, null); }
    public boolean isExpanded() {
        return expanded;
    }
    public void setExpanded(boolean isExpanded) {
        expanded = isExpanded;
    }
    public int getUniqueId() {
        return uniqueId;
    }
}
