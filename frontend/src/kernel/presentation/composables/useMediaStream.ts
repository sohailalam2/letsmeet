import {onBeforeUnmount, ref, watch, type Ref} from 'vue';

/**
 * Composable for managing media streams (camera and microphone)
 * with proper resource cleanup and device switching
 */
export function useMediaStream(
    videoRef: Ref<HTMLVideoElement | null>,
    camera: Ref<string | null>,
    microphone: Ref<string | null>
) {
    // Stream state
    const isCameraOn = ref(false);
    const isMicOn = ref(false);

    // Track active streams and tracks for proper cleanup
    let activeVideoTracks: MediaStreamTrack[] = [];
    let activeAudioTracks: MediaStreamTrack[] = [];

    /**
     * Stops all tracks of a specific type and clears the tracking arrays
     */
    const stopAllTracks = (type: 'video' | 'audio' | 'all'): void => {
        if (type === 'video' || type === 'all') {
            activeVideoTracks.forEach(track => {
                if (track.readyState === 'live') {
                    track.stop();
                }
            });
            activeVideoTracks = [];
        }

        if (type === 'audio' || type === 'all') {
            activeAudioTracks.forEach(track => {
                if (track.readyState === 'live') {
                    track.stop();
                }
            });
            activeAudioTracks = [];
        }
    };

    /**
     * Updates the video element with current active tracks
     */
    const updateVideoStream = (): void => {
        if (!videoRef.value) return;

        // Get the old stream to clean up
        const oldStream = videoRef.value.srcObject as MediaStream | null;

        // Create a new stream with active tracks
        const tracks = [...activeVideoTracks, ...activeAudioTracks];

        if (tracks.length > 0) {
            videoRef.value.srcObject = new MediaStream(tracks);
        } else {
            videoRef.value.srcObject = null;
        }

        // If we had an old stream and it's different from the new one, clean it up
        if (oldStream && oldStream !== videoRef.value.srcObject) {
            // We don't stop the tracks here as they might have been transferred
            // to the new stream. We just release the reference.
            oldStream.getTracks().forEach(track => {
                oldStream.removeTrack(track);
            });
        }
    };

    /**
     * Toggle camera on/off
     */
    const toggleCamera = async (): Promise<void> => {
        try {
            if (!isCameraOn.value) {
                // Stop any existing video tracks first
                stopAllTracks('video');

                // Get new camera stream with selected device
                const constraints = {
                    video: camera.value ? { deviceId: { exact: camera.value } } : true
                };

                const stream = await navigator.mediaDevices.getUserMedia(constraints);

                // Store the new video tracks
                activeVideoTracks = Array.from(stream.getVideoTracks());

                // Update the video element
                updateVideoStream();
                isCameraOn.value = true;
            } else {
                // Stop all video tracks
                stopAllTracks('video');

                // Update the video element
                updateVideoStream();
                isCameraOn.value = false;
            }
        } catch (err) {
            console.error('Camera access failed:', err);
            isCameraOn.value = false;

            // Show user-friendly error
            const errorMessage = err instanceof DOMException && err.name === 'NotAllowedError'
                ? 'Camera access denied. Please check your browser permissions.'
                : 'Failed to access camera. Please make sure your camera is connected and not in use by another application.';

            alert(errorMessage);
        }
    };

    /**
     * Toggle microphone on/off
     */
    const toggleMic = async (): Promise<void> => {
        try {
            if (!isMicOn.value) {
                // Stop any existing audio tracks first
                stopAllTracks('audio');

                // Get new mic stream with selected device
                const constraints = {
                    audio: microphone.value ? { deviceId: { exact: microphone.value } } : true
                };

                const stream = await navigator.mediaDevices.getUserMedia(constraints);

                // Store the new audio tracks
                activeAudioTracks = Array.from(stream.getAudioTracks());

                // Update the video element
                updateVideoStream();
                isMicOn.value = true;
            } else {
                // Stop all audio tracks
                stopAllTracks('audio');

                // Update the video element
                updateVideoStream();
                isMicOn.value = false;
            }
        } catch (err) {
            console.error('Microphone access failed:', err);
            isMicOn.value = false;

            // Show user-friendly error
            const errorMessage = err instanceof DOMException && err.name === 'NotAllowedError'
                ? 'Microphone access denied. Please check your browser permissions.'
                : 'Failed to access microphone. Please make sure your microphone is connected and not in use by another application.';

            alert(errorMessage);
        }
    };

    /**
     * Update device selection for active streams
     */
    const updateDeviceSelection = async (
        type: 'audio' | 'video',
        deviceId: string
    ): Promise<void> => {
        try {
            if (type === 'video' && isCameraOn.value) {
                // Stop existing video tracks
                stopAllTracks('video');

                // Create new stream with selected device
                const stream = await navigator.mediaDevices.getUserMedia({
                    video: { deviceId: { exact: deviceId } }
                });

                // Store the new video tracks
                activeVideoTracks = Array.from(stream.getVideoTracks());

                // Update the video element
                updateVideoStream();
            } else if (type === 'audio' && isMicOn.value) {
                // Stop existing audio tracks
                stopAllTracks('audio');

                // Create new stream with selected device
                const stream = await navigator.mediaDevices.getUserMedia({
                    audio: { deviceId: { exact: deviceId } }
                });

                // Store the new audio tracks
                activeAudioTracks = Array.from(stream.getAudioTracks());

                // Update the video element
                updateVideoStream();
            }
        } catch (err) {
            console.error(`Failed to switch ${type} device:`, err);
            alert(`Failed to switch ${type} device. The device may be disconnected or in use by another application.`);
        }
    };

    /**
     * Clean up all media streams
     */
    const cleanup = (): void => {
        // Stop all tracks
        stopAllTracks('all');

        // Ensure video element's srcObject is nullified to release references
        if (videoRef.value && videoRef.value.srcObject) {
            const oldStream = videoRef.value.srcObject as MediaStream;

            // Remove all tracks from the stream
            oldStream.getTracks().forEach(track => {
                oldStream.removeTrack(track);
            });

            // Clear the srcObject
            videoRef.value.srcObject = null;
        }

        isCameraOn.value = false;
        isMicOn.value = false;
    };

    // Watch for device changes when streams are active
    watch(camera, async (newDeviceId, oldDeviceId) => {
        if (newDeviceId && newDeviceId !== oldDeviceId && isCameraOn.value) {
            await updateDeviceSelection('video', newDeviceId);
        }
    });

    watch(microphone, async (newDeviceId, oldDeviceId) => {
        if (newDeviceId && newDeviceId !== oldDeviceId && isMicOn.value) {
            await updateDeviceSelection('audio', newDeviceId);
        }
    });

    // Ensure cleanup on component unmount
    onBeforeUnmount(cleanup);

    return {
        isCameraOn,
        isMicOn,
        toggleCamera,
        toggleMic,
        updateDeviceSelection,
        cleanup
    };
}
