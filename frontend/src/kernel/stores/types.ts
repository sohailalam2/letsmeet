import type { VideoTileState } from "amazon-chime-sdk-js";

export interface StagingDevices {
  audioInput?: string;
  audioOutput?: string;
  videoInput?: string;
}
export interface VideoTileInfo {
  tileState: VideoTileState;
  videoElement: HTMLVideoElement;
  tileContainer: HTMLDivElement;
}
export interface SpotLightTileInfo{
  tileId: number;
  tileContainer: HTMLElement;
}