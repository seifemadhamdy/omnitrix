package seifemadhamdy.omnitrix.view

import android.animation.*
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.core.content.ContextCompat.getColor
import androidx.core.graphics.ColorUtils
import androidx.core.os.postDelayed
import seifemadhamdy.omnitrix.R
import seifemadhamdy.omnitrix.constant.*

class OmnitrixView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val hourglassTopShapeImageView by lazy { ImageView(context) }
    private val hourglassBottomShapeImageView by lazy { ImageView(context) }
    private val selectorStartChevronImageView by lazy { ImageView(context) }
    private val selectorEndChevronImageView by lazy { ImageView(context) }
    private val selectorInnerShapeImageView by lazy { ImageView(context) }
    private val selectorStartFrameImageView by lazy { ImageView(context) }
    private val selectorEndFrameImageView by lazy { ImageView(context) }
    private lateinit var navigation: Navigation
    private var mode = Mode.ACTIVE
    private var objectAnimators = ArrayList<ObjectAnimator>()
    private var animatorSet: AnimatorSet? = null
    private var valueAnimator: ValueAnimator? = null
    private var mediaPlayer: MediaPlayer? = null
    private var idleMediaPlayer: MediaPlayer? = null
    private var alienSelectionIndex = 0
    private val aliens = arrayListOf(
        R.drawable.heatblast,
        R.drawable.wildmutt,
        R.drawable.diamondhead,
        R.drawable.xlr8,
        R.drawable.grey_matter,
        R.drawable.four_arms,
        R.drawable.stinkfly,
        R.drawable.ripjaws,
        R.drawable.upgrade,
        R.drawable.ghostfreak,
        R.drawable.cannonbolt,
        R.drawable.wildvine,
        R.drawable.blitzwolfer,
        R.drawable.upchuck,
        R.drawable.snare_oh,
        R.drawable.frankenstrike,
        R.drawable.eye_guy,
        R.drawable.way_big,
        R.drawable.ditto
    )

    init {
        hourglassTopShapeImageView.apply {
            visibility = View.INVISIBLE
            prepare(R.drawable.hourglass_top_shape)
        }

        hourglassBottomShapeImageView.apply {
            visibility = View.INVISIBLE
            prepare(R.drawable.hourglass_bottom_shape)
        }

        selectorStartChevronImageView.prepare(R.drawable.selector_start_chevron)
        selectorEndChevronImageView.prepare(R.drawable.selector_end_chevron)

        selectorInnerShapeImageView.apply {
            visibility = View.INVISIBLE
            prepare(null)
        }

        selectorStartFrameImageView.prepare(R.drawable.selector_start_frame)
        selectorEndFrameImageView.prepare(R.drawable.selector_end_frame)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        setViewsInitialTranslation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_UP) {
            (width / 3).apply {
                when (event.x.toInt()) {
                    in 0..this -> {
                        navigation = Navigation.RETREAT
                        performClick()
                    }

                    in this..(this * 2) -> {
                        navigation = Navigation.SELECT
                        performClick()
                    }

                    in (this * 2)..(this * 3) -> {
                        navigation = Navigation.ADVANCE
                        performClick()
                    }
                }
            }
        }

        return true
    }

    override fun performClick(): Boolean {
        super.performClick()

        when (mode) {
            Mode.ACTIVE -> {
                keepScreenOn = true
                selectingMode()
            }
            Mode.SELECTING -> {
                inactiveMode(navigation)
            }
            Mode.INACTIVE -> {
                rechargingMode()
            }
            Mode.RECHARGING -> {
                activeMode()
                keepScreenOn = false
            }
        }

        return true
    }

    private fun ImageView.prepare(
        resId: Int? = null
    ) {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )

        scaleType = ScaleType.CENTER_CROP

        if (resId != null) {
            setImageResource(resId)
        }

        addView(this)
    }

    private fun setViewsInitialTranslation() {
        (width / 2).toFloat().apply {
            selectorStartChevronImageView.translationX = -this
            selectorEndChevronImageView.translationX = this
            selectorStartFrameImageView.translationX = -this
            selectorEndFrameImageView.translationX = this
        }
    }

    private fun selectingMode() {
        if (isEnabled) {
            isEnabled = false

            play(getRandomSound(Sound.ACTIVATION_IDS)) {
                play(Sound.HOURGLASS_DISPLAYING_ALIENS_ID) {
                    mode = Mode.SELECTING
                    isEnabled = true
                }


                objectAnimators.apply {
                    add(
                        ObjectAnimator.ofFloat(
                            hourglassTopShapeImageView,
                            "y",
                            (-height / 2).toFloat()
                        )
                            .apply {
                                startDelay = Duration.HOURGLASS_SHAPE_ANIMATION_START_DELAY
                                duration =
                                    getMediaPlayerTrackDuration() - Duration.HOURGLASS_SHAPE_ANIMATION_START_DELAY
                            }
                    )

                    add(
                        ObjectAnimator.ofFloat(
                            hourglassBottomShapeImageView,
                            "y",
                            (height / 2).toFloat()
                        )
                            .apply {
                                startDelay = Duration.HOURGLASS_SHAPE_ANIMATION_START_DELAY
                                duration =
                                    getMediaPlayerTrackDuration() - Duration.HOURGLASS_SHAPE_ANIMATION_START_DELAY
                            }
                    )

                    add(
                        ObjectAnimator.ofFloat(selectorStartChevronImageView, "scaleX", 1f).apply {
                            startDelay = Duration.SELECTOR_INNER_SHAPE_ANIMATION_START_DELAY
                            duration = getMediaPlayerTrackDuration() - startDelay
                        }
                    )

                    add(
                        ObjectAnimator.ofFloat(selectorStartChevronImageView, "scaleY", 1f).apply {
                            startDelay = Duration.SELECTOR_INNER_SHAPE_ANIMATION_START_DELAY
                            duration = getMediaPlayerTrackDuration() - startDelay
                        }
                    )

                    add(
                        ObjectAnimator.ofFloat(selectorEndChevronImageView, "scaleX", 1f).apply {
                            startDelay = Duration.SELECTOR_INNER_SHAPE_ANIMATION_START_DELAY
                            duration = getMediaPlayerTrackDuration() - startDelay
                        }
                    )

                    add(
                        ObjectAnimator.ofFloat(selectorEndChevronImageView, "scaleY", 1f).apply {
                            startDelay = Duration.SELECTOR_INNER_SHAPE_ANIMATION_START_DELAY
                            duration = getMediaPlayerTrackDuration() - startDelay
                        }
                    )

                    add(
                        ObjectAnimator.ofFloat(selectorStartFrameImageView, "x", 0f).apply {
                            duration = getMediaPlayerTrackDuration()
                        }
                    )

                    add(
                        ObjectAnimator.ofFloat(selectorEndFrameImageView, "x", 0f).apply {
                            duration = getMediaPlayerTrackDuration()
                        }
                    )
                }

                animatorSet = AnimatorSet().apply {
                    playTogether(
                        *objectAnimators.toArray(
                            arrayOfNulls<ObjectAnimator>(objectAnimators.size)
                        )
                    )

                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            super.onAnimationStart(animation)

                            Handler(Looper.getMainLooper()).postDelayed(
                                Duration.HOURGLASS_SHAPE_ANIMATION_START_DELAY
                            ) {
                                hourglassTopShapeImageView.setImageResource(R.drawable.cutout_top_shape)
                                hourglassBottomShapeImageView.setImageResource(R.drawable.cutout_bottom_shape)
                            }

                            hourglassTopShapeImageView.visibility = View.VISIBLE
                            hourglassBottomShapeImageView.visibility = View.VISIBLE

                            selectorStartChevronImageView.apply {
                                scaleX = 0f
                                scaleY = 0f
                                translationX = 0f

                                setColorFilter(
                                    getColor(
                                        context,
                                        R.color.omnitrix_green
                                    )
                                )
                            }

                            selectorEndChevronImageView.apply {
                                scaleX = 0f
                                scaleY = 0f
                                translationX = 0f

                                setColorFilter(
                                    getColor(
                                        context,
                                        R.color.omnitrix_green
                                    )
                                )
                            }

                            setBackgroundColor(
                                getColor(
                                    context,
                                    R.color.omnitrix_grey
                                )
                            )
                        }
                    })

                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            hourglassTopShapeImageView.apply {
                                visibility = View.INVISIBLE
                                setImageResource(R.drawable.hourglass_top_shape)
                            }

                            hourglassBottomShapeImageView.apply {
                                visibility = View.INVISIBLE
                                setImageResource(R.drawable.hourglass_bottom_shape)
                            }

                            selectorStartFrameImageView.visibility = View.INVISIBLE
                            selectorEndFrameImageView.visibility = View.INVISIBLE

                            aliens.shuffle()
                            alienSelectionIndex = 0

                            selectorInnerShapeImageView.apply {
                                visibility = View.VISIBLE
                                setImageResource(aliens[alienSelectionIndex])
                            }

                            selectorStartChevronImageView.clearColorFilter()
                            selectorEndChevronImageView.clearColorFilter()
                            setViewsInitialTranslation()
                            playIdle()
                        }
                    })

                    start()
                }
            }
        }
    }

    private fun inactiveMode(navigation: Navigation) {
        when (navigation) {
            Navigation.RETREAT -> {
                navigateToAlien(advance = false)
            }

            Navigation.SELECT -> {
                if (isEnabled) {
                    releaseIdleMediaPlayer()
                    isEnabled = false

                    play(getRandomSound(Sound.TRANSFORMATION_IDS)) {
                        mode = Mode.INACTIVE
                        isEnabled = true
                    }

                    valueAnimator = ValueAnimator.ofObject(
                        ArgbEvaluator(),
                        getColor(context, R.color.omnitrix_green),
                        ColorUtils.setAlphaComponent(
                            getColor(
                                context,
                                R.color.omnitrix_green
                            ), 0
                        )
                    ).apply {
                        interpolator = DecelerateInterpolator()
                        duration = getMediaPlayerTrackDuration()

                        addUpdateListener {
                            foreground =
                                ColorDrawable((it.animatedValue as Int))
                        }

                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationStart(animation: Animator) {
                                super.onAnimationStart(animation)
                                setBackgroundColor(
                                    getColor(
                                        context,
                                        R.color.omnitrix_white
                                    )
                                )

                                selectorInnerShapeImageView.visibility =
                                    View.INVISIBLE

                                selectorStartFrameImageView.visibility =
                                    View.VISIBLE
                                selectorEndFrameImageView.visibility =
                                    View.VISIBLE
                            }
                        })

                        start()
                    }
                }
            }

            Navigation.ADVANCE -> {
                navigateToAlien(advance = true)
            }
        }
    }

    private fun activeMode() {
        if (isEnabled) {
            isEnabled = false

            play(Sound.RECHARGE_ID) {
                mode = Mode.ACTIVE
                isEnabled = true
            }

            valueAnimator = ValueAnimator.ofObject(
                ArgbEvaluator(),
                getColor(context, R.color.omnitrix_red),
                getColor(context, R.color.omnitrix_green)
            ).apply {
                duration = getMediaPlayerTrackDuration()

                addUpdateListener {
                    setBackgroundColor(it.animatedValue as Int)
                }

                start()
            }
        }
    }

    private fun rechargingMode() {
        if (isEnabled) {
            isEnabled = false

            play(Sound.TIMEOUT_WARNING_ID)

            valueAnimator = ValueAnimator.ofObject(
                ArgbEvaluator(),
                getColor(context, R.color.omnitrix_white),
                getColor(context, R.color.omnitrix_red)
            ).apply {
                repeatMode = ValueAnimator.REVERSE
                repeatCount = Count.TIMEOUT_TICKS
                duration = getMediaPlayerTrackDuration() / Count.TIMEOUT_TICKS

                addUpdateListener {
                    setBackgroundColor(it.animatedValue as Int)
                }

                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)

                        play(
                            getRandomSound(Sound.DETRANSFORMATION_IDS)
                        ) {
                            mode = Mode.RECHARGING
                            isEnabled = true
                        }

                        valueAnimator = ValueAnimator.ofObject(
                            ArgbEvaluator(),
                            getColor(context, R.color.omnitrix_red),
                            ColorUtils.setAlphaComponent(
                                getColor(context, R.color.omnitrix_red), 0
                            )
                        ).apply {
                            interpolator = DecelerateInterpolator()
                            duration = getMediaPlayerTrackDuration()

                            addUpdateListener {
                                foreground =
                                    ColorDrawable((it.animatedValue as Int))
                            }

                            addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationStart(animation: Animator) {
                                    super.onAnimationStart(animation)

                                    setBackgroundColor(
                                        getColor(
                                            context,
                                            R.color.omnitrix_red
                                        )
                                    )
                                }
                            })

                            start()
                        }
                    }
                })

                start()
            }
        }
    }

    private fun play(
        resId: Int,
        doOnCompletion: () -> Unit
    ) {
        releaseMediaPlayer()

        mediaPlayer = MediaPlayer.create(context, resId).apply {
            start()

            setOnCompletionListener {
                releaseMediaPlayer()
                doOnCompletion()
            }
        }
    }

    private fun getRandomSound(soundIds: ArrayList<Int>) = soundIds[(soundIds.indices).random()]

    private fun getMediaPlayerTrackDuration() = mediaPlayer?.duration?.toLong() ?: 0L

    private fun playIdle() {
        releaseIdleMediaPlayer()

        idleMediaPlayer = MediaPlayer.create(context, Sound.IDLE_ID).apply {
            isLooping = true
            start()
        }
    }

    private fun navigateToAlien(advance: Boolean) {
        play(getRandomSound(Sound.DIAL_TURNING_IDS))

        if (advance) {
            if (alienSelectionIndex != aliens.size - 1) {
                alienSelectionIndex++
            } else {
                alienSelectionIndex = 0
            }
        } else {
            if (alienSelectionIndex != 0) {
                alienSelectionIndex--
            } else {
                alienSelectionIndex = aliens.size - 1
            }
        }

        showAlien()
    }

    private fun releaseIdleMediaPlayer() {
        if (idleMediaPlayer != null) {
            idleMediaPlayer?.release()
            idleMediaPlayer = null
        }
    }

    private fun play(
        resId: Int
    ) {
        releaseMediaPlayer()

        mediaPlayer = MediaPlayer.create(context, resId).apply {
            start()

            setOnCompletionListener {
                releaseMediaPlayer()
            }
        }
    }

    private fun releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    private fun showAlien(resourceId: Int = aliens[alienSelectionIndex]) {
        selectorInnerShapeImageView.setImageResource(resourceId)
    }

    private fun resumeAnimator(vararg animatorsToResume: Animator?) {
        for (animatorToResume in animatorsToResume)
            animatorToResume?.resume()
    }

    private fun startMediaPlayer(vararg mediaPlayersToStart: MediaPlayer?) {
        for (mediaPlayerToStart in mediaPlayersToStart)
            mediaPlayerToStart?.start()
    }

    fun resume() {
        resumeAnimator(valueAnimator, animatorSet)
        startMediaPlayer(mediaPlayer, idleMediaPlayer)
    }

    private fun pauseAnimator(vararg animatorsToPause: Animator?) {
        for (animatorToPause in animatorsToPause)
            animatorToPause?.apply {
                if (isRunning) {
                    pause()
                } else {
                    when (this) {
                        is ValueAnimator -> valueAnimator = null
                        is AnimatorSet -> animatorSet = null
                    }
                }
            }
    }

    private fun pauseMediaPlayer() {
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
            } else {
                releaseMediaPlayer()
            }
        }
    }

    private fun pauseIdleMediaPlayer() {
        idleMediaPlayer?.apply {
            if (isPlaying) {
                pause()
            } else {
                releaseIdleMediaPlayer()
            }
        }
    }

    fun pause() {
        pauseAnimator(valueAnimator, animatorSet)
        pauseMediaPlayer()
        pauseIdleMediaPlayer()
    }

    private fun destroyAnimator(vararg animatorsToDestroy: Animator?) {
        for (animatorToDestroy in animatorsToDestroy)
            animatorToDestroy?.apply {
                if (isRunning) {
                    cancel()
                }

                when (this) {
                    is ValueAnimator -> valueAnimator = null
                    is AnimatorSet -> animatorSet = null
                }
            }
    }

    fun destroy() {
        destroyAnimator(valueAnimator, animatorSet)
        releaseMediaPlayer()
        releaseIdleMediaPlayer()
    }
}