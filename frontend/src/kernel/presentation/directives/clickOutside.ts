import { type DirectiveBinding } from 'vue';

export const clickOutside = {
    mounted(el: HTMLElement, binding: DirectiveBinding) {
        el._clickOutsideHandler = (event: Event) => {
            if (!(el === event.target || el.contains(event.target as Node))) {
                binding.value(event);
            }
        };
        document.addEventListener('click', el._clickOutsideHandler);
    },

    unmounted(el: HTMLElement) {
        document.removeEventListener('click', el._clickOutsideHandler);
        delete el._clickOutsideHandler;
    }
};

// Add type declaration
declare module '@vue/runtime-core' {
    interface ComponentCustomProperties {
        _clickOutsideHandler: (event: Event) => void;
    }
}
