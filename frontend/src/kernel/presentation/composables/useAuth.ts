import {computed, onMounted, ref} from 'vue';
import {AuthService, type UserTokenPayload, useToast} from "@/kernel";

const {showSuccessToast, showErrorToast} = useToast();

export function useAuth() {
    const userInfo = ref<UserTokenPayload | null>(null);
    const imageUrl = ref<string | undefined>(undefined);
    const authLoginUri = ref<string | undefined>(undefined);

    const isAuthenticated = computed(() => !!userInfo.value);

    onMounted(() => {
        // Initialize auth state
        authLoginUri.value = AuthService.getLoginUri();

        if (AuthService.isUserLoggedIn()) {
            userInfo.value = AuthService.getUserContext();
            imageUrl.value = userInfo.value?.picture;
        }
    });

    const login = async () => {
        // Implement login logic if needed
        window.location.href = authLoginUri.value || '';
    };

    const logout = async () => {
        try {
            await AuthService.logout();
            userInfo.value = null;
            imageUrl.value = undefined;
            showSuccessToast("Logged Out Successfully");
        } catch (e) {
            showErrorToast("Something went wrong. Can you please try again");
        }
    };

    const handleAuthCode = async (code: string) => {
        try {
            await AuthService.loginWithCode(code);
            showSuccessToast("Logged In Successfully 🎉🎉");
            // Update user info after successful login
            userInfo.value = AuthService.getUserContext();
            imageUrl.value = userInfo.value?.picture;
        } catch (e) {
            showErrorToast("Something went wrong. Can you please try again");
        }
    };

    return {
        userInfo,
        imageUrl,
        authLoginUri,
        isAuthenticated,
        login,
        logout,
        handleAuthCode
    };
}
