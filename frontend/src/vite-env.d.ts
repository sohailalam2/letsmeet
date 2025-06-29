/// <reference types="vite/client" />
interface ImportMetaEnv {
  readonly VITE_LETSMEET_API_BASE: string;
}

interface ImportMeta {
  // eslint-disable-next-line @typescript-eslint/naming-convention
  readonly env: ImportMetaEnv;
}
