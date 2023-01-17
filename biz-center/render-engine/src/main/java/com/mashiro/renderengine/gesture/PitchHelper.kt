package com.mashiro.renderengine.gesture

/**
 * 由于filament没有实现俯仰角约束功能，因此要考虑自己实现
 * 计算方式参考OrbitManipulator.h
    if (mGrabState == ORBITING) {
        Bookmark bookmark = getCurrentBookmark();

        const FLOAT theta = delx * Base::mProps.orbitSpeed.x;
        const FLOAT phi = dely * Base::mProps.orbitSpeed.y;
        const FLOAT maxPhi = MAX_PHI;

        bookmark.orbit.phi = clamp(mGrabBookmark.orbit.phi + phi, -maxPhi, +maxPhi);
        bookmark.orbit.theta = mGrabBookmark.orbit.theta + theta;

        jumpToBookmark(bookmark);
    }
 */
class PitchHelper {
    private var startPosition = -1f


    fun init(currentY: Float){
        startPosition = currentY
    }

    fun calculatePhi(currentPhi: Float, currentY: Float, orbitSpeed: Float): Float {
        return currentPhi + (currentY - startPosition) * orbitSpeed
    }

    fun release(){
        startPosition = -1f
    }
}