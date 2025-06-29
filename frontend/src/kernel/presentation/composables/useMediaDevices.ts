import { ref, onMounted } from 'vue';

export function useMediaDevices() {
    // Available devices
    const cameras = ref<MediaDeviceInfo[]>([]);
    const microphones = ref<MediaDeviceInfo[]>([]);
    const speakers = ref<MediaDeviceInfo[]>([]);

    // Selected devices
    const camera = ref<string | null>(null);
    const microphone = ref<string | null>(null);
    const speaker = ref<string | null>(null);

    /**
     * Request permissions and enumerate all media devices
     */
    const refreshDevices = async (): Promise<void> => {
        try {
            // Request permissions first to ensure we get labeled devices
            // This is especially important for Firefox
            await navigator.mediaDevices.getUserMedia({ audio: true, video: true })
                .catch(err => {
                    // If full permissions fail, try with just audio
                    return navigator.mediaDevices.getUserMedia({ audio: true });
                })
                .catch(err => {
                    // If audio fails, try with just video
                    return navigator.mediaDevices.getUserMedia({ video: true });
                })
                .catch(err => {
                    // If all fail, continue but devices may be unlabeled
                    console.warn('Could not get media permissions:', err);
                });

            // Enumerate devices
            const devices = await navigator.mediaDevices.enumerateDevices();

            // Filter devices by type
            const cams = devices.filter(device => device.kind === 'videoinput');
            const mics = devices.filter(device => device.kind === 'audioinput');
            const spks = devices.filter(device => device.kind === 'audiooutput');

            // Update refs
            cameras.value = cams;
            microphones.value = mics;
            speakers.value = spks;

            // Set default selections if not already set
            if (!camera.value && cams.length > 0) {
                camera.value = cams[0].deviceId;
            }

            if (!microphone.value && mics.length > 0) {
                microphone.value = mics[0].deviceId;
            }

            if (!speaker.value && spks.length > 0) {
                speaker.value = spks[0].deviceId;
            }

        } catch (error) {
            console.error('Error accessing media devices:', error);
        }
    };

    onMounted(refreshDevices);

    return {
        cameras,
        microphones,
        speakers,
        camera,
        microphone,
        speaker,
        refreshDevices
    };
}
