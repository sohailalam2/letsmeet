import "./assets/styles/base.css";

import { createApp } from "vue";
import type { RouteRecordRaw } from "vue-router";
import { createRouter, createWebHistory } from "vue-router";
import { createPinia } from "pinia";

import App from "./kernel/presentation/core/App.vue";
import {clickOutside} from "./kernel/presentation/directives";

const app = createApp(App);

const routes: RouteRecordRaw[] = Object.values(
  import.meta.glob("/src/pages/**/routes.json", {
    eager: true,
  }) as Record<string, { default: RouteRecordRaw[] }>
).reduce((arr, item) => arr.concat(item.default), [] as RouteRecordRaw[]);

const components = import.meta.glob("/src/pages/**/*.vue", {
  eager: true,
}) as Record<string, { default: any }>;

Object.entries(components).forEach(([path, comp]) => {
  const component = comp.default;
  const name = component.name || path.split("/").pop()?.replace(".vue", "");

  if (name) {
    const route = routes.find((r) => r.component === name);

    app.component(name, component);
    if (route) {
      route.component = component;
    }
  }
});

app
    .use(createPinia())
    .use(createRouter({
      history: createWebHistory(),
      routes,
    }))
    .directive('click-outside', clickOutside)
    .mount("#app");
