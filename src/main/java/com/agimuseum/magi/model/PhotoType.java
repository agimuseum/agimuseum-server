package com.agimuseum.magi.model;

/**
 * Enum to distinguish between different types of photos in the system
 */
public enum PhotoType {
    /**
     * A stock photo of a location or stop that will be shown in the app
     * These photos are not related to visit proof
     */
    STOCK,

    /**
     * A photo uploaded by a user as proof of visiting a location or stop
     * These photos are not shown as stock photos
     */
    VISIT_PROOF,

    /**
     * A photo uploaded for verification purposes (admin use)
     */
    VERIFICATION,

    /**
     * A photo uploaded for a reward claim
     */
    REWARD_CLAIM
}