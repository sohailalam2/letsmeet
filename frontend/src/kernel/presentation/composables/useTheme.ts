import { ref, onMounted } from "vue";

const theme = ref<"light" | "dark">("light");

export function useTheme() {
  const toggleTheme = () => {
    theme.value = theme.value === "light" ? "dark" : "light";
    document.documentElement.classList.toggle("dark", theme.value === "dark");
    localStorage.setItem("theme", theme.value);
  };

  onMounted(() => {
    const saved = localStorage.getItem("theme") as "light" | "dark" | null;
    if (saved) {
      theme.value = saved;
      document.documentElement.classList.toggle("dark", saved === "dark");
    } else {
      const prefersDark = window.matchMedia(
        "(prefers-color-scheme: dark)"
      ).matches;
      theme.value = prefersDark ? "dark" : "light";
      document.documentElement.classList.toggle("dark", prefersDark);
    }
  });

  return { theme, toggleTheme };
}

