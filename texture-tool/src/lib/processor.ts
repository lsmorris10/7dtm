import { nearestPaletteColor } from "./palette";

export interface ProcessingOptions {
  shrinkFactor?: number;
  vignetteStrength?: number;
  quantize?: boolean;
}

export async function processImageToTexture(
  source: HTMLImageElement | HTMLCanvasElement,
  options: ProcessingOptions = {}
): Promise<Blob> {
  const { shrinkFactor = 0.85, vignetteStrength = 0.3, quantize = true } = options;

  const srcWidth = source instanceof HTMLImageElement ? source.naturalWidth : source.width;
  const srcHeight = source instanceof HTMLImageElement ? source.naturalHeight : source.height;

  const tempCanvas = document.createElement("canvas");
  tempCanvas.width = srcWidth;
  tempCanvas.height = srcHeight;
  const tempCtx = tempCanvas.getContext("2d")!;

  tempCtx.clearRect(0, 0, srcWidth, srcHeight);

  const shrunkW = Math.round(srcWidth * shrinkFactor);
  const shrunkH = Math.round(srcHeight * shrinkFactor);
  const offsetX = Math.round((srcWidth - shrunkW) / 2);
  const offsetY = Math.round((srcHeight - shrunkH) / 2);
  tempCtx.drawImage(source, offsetX, offsetY, shrunkW, shrunkH);

  if (vignetteStrength > 0) {
    const cx = srcWidth / 2;
    const cy = srcHeight / 2;
    const innerRadius = Math.min(srcWidth, srcHeight) * 0.3;
    const outerRadius = Math.max(srcWidth, srcHeight) * 0.75;
    const gradient = tempCtx.createRadialGradient(cx, cy, innerRadius, cx, cy, outerRadius);
    gradient.addColorStop(0, `rgba(0,0,0,0)`);
    gradient.addColorStop(1, `rgba(0,0,0,${vignetteStrength})`);
    tempCtx.fillStyle = gradient;
    tempCtx.fillRect(0, 0, srcWidth, srcHeight);
  }

  const outputCanvas = document.createElement("canvas");
  outputCanvas.width = 16;
  outputCanvas.height = 16;
  const outputCtx = outputCanvas.getContext("2d")!;
  outputCtx.imageSmoothingEnabled = false;
  outputCtx.drawImage(tempCanvas, 0, 0, 16, 16);

  if (quantize) {
    const imageData = outputCtx.getImageData(0, 0, 16, 16);
    const data = imageData.data;
    for (let i = 0; i < data.length; i += 4) {
      const a = data[i + 3];
      if (a < 128) {
        data[i] = 0;
        data[i + 1] = 0;
        data[i + 2] = 0;
        data[i + 3] = 0;
        continue;
      }
      const [r, g, b] = nearestPaletteColor(data[i], data[i + 1], data[i + 2]);
      data[i] = r;
      data[i + 1] = g;
      data[i + 2] = b;
      data[i + 3] = 255;
    }
    outputCtx.putImageData(imageData, 0, 0);
  }

  return new Promise((resolve, reject) => {
    outputCanvas.toBlob(
      (blob) => {
        if (blob) resolve(blob);
        else reject(new Error("Failed to export canvas to blob"));
      },
      "image/png",
      1.0
    );
  });
}

export function loadImageFromFile(file: File): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file);
    const img = new Image();
    img.onload = () => {
      URL.revokeObjectURL(url);
      resolve(img);
    };
    img.onerror = () => {
      URL.revokeObjectURL(url);
      reject(new Error("Failed to load image"));
    };
    img.src = url;
  });
}

export function loadImageFromURL(url: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.crossOrigin = "anonymous";
    img.onload = () => resolve(img);
    img.onerror = () => reject(new Error("Failed to load image from URL"));
    img.src = url;
  });
}

export function blobToDataURL(blob: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = reject;
    reader.readAsDataURL(blob);
  });
}
