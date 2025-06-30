import { type DirectiveBinding } from 'vue';

// Extend HTMLElement to allow custom property
interface ClickOutsideElement extends HTMLElement {
  _clickOutsideHandler?: (event: Event) => void;
}

export const clickOutside = {
  mounted(el: ClickOutsideElement, binding: DirectiveBinding) {
    el._clickOutsideHandler = (event: Event) => {
      if (!(el === event.target || el.contains(event.target as Node))) {
        binding.value(event);
      }
    };
    document.addEventListener('click', el._clickOutsideHandler);
  },

  unmounted(el: ClickOutsideElement) {
    if (el._clickOutsideHandler) {
      document.removeEventListener('click', el._clickOutsideHandler);
      delete el._clickOutsideHandler;
    }
  }
};
