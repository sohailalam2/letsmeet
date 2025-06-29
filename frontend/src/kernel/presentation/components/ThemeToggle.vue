<template>
  <div class="cursor-pointer">
    <div class="daynighttogglebutton cursor-pointer">
      <label
        class="relative flex justify-between items-center group p-2 w-fit ml-auto mr-0.5 cursor-pointer"
      >
        <input
          type="checkbox"
          class="themeinputs hidden absolute left-10 w-0 h-0 peer appearance-none rounded-md cursor-pointer"
          id="inputs2"
          v-model="isChecked"
          @click="toggleTheme"
        />
        <span
          class="w-24 h-8 flex items-center flex-shrink-0 ml-4 py-1 after:ml-1 peer-checked:pl-2.5 pr-2.5 bg-gray-300 bg-[url('/assets/images/profile/moon.png')] bg-right peer-checked:bg-[url('/assets/images/profile/sun.png')] bg-no-repeat bg-origin-content peer-checked:bg-left duration-300 rounded-full ease-in-out peer-checked:after:bg-[url('/assets/images/profile/moon.png')] after:w-12 after:h-6 after:bg-[url('/assets/images/profile/sun.png')] after:bg-no-repeat after:bg-white after:bg-center after:rounded-full after:shadow-md after:duration-300 peer-checked:after:translate-x-[1.8rem] group-hover:after:translate-x-1 group-hover:peer-checked:after:translate-x-[1.6rem] cursor-pointer"
        ></span>
      </label>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import {useTheme} from "@/kernel";

const { theme, toggleTheme } = useTheme();
const isChecked = ref(true);

onMounted(() => {
  isChecked.value = theme.value !== "light";
});
</script>
