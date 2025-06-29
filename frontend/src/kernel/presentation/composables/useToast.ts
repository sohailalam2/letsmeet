type ToastType = "success" | "error" | "warning" | "info";

export function useToast() {
    const getContainer = () =>
        document.getElementById("toast-container");

    const showToast = (message: string, type: ToastType = "info") => {
        const container = getContainer();
        if (!container) {
            console.warn("Toast container not found");
            return;
        }

        const toast = document.createElement("div");
        toast.className = `mb-3 p-3 rounded-lg shadow-lg max-w-xs animate-fade-in-down`;
        toast.textContent = message;

        const baseClasses = ["text-white"];
        switch (type) {
            case "success":
                toast.classList.add("bg-green-500", ...baseClasses);
                break;
            case "error":
                toast.classList.add("bg-red-500", ...baseClasses);
                break;
            case "warning":
                toast.classList.add("bg-yellow-500", ...baseClasses);
                break;
            case "info":
            default:
                toast.classList.add("bg-blue-500", ...baseClasses);
                break;
        }

        container.appendChild(toast);

        setTimeout(() => {
            toast.classList.add("animate-fade-out");
            setTimeout(() => toast.remove(), 200);
        }, 5000);
    };

    return {
        showToast,
        showSuccessToast: (msg: string) => showToast(msg, "success"),
        showErrorToast: (msg: string) => showToast(msg, "error"),
        showWarningToast: (msg: string) => showToast(msg, "warning"),
        showInfoToast: (msg: string) => showToast(msg, "info"),
    };
}
