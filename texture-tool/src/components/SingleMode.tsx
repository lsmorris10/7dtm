import { useState, useCallback, useRef } from "react";
import {
  processImageToTexture,
  loadImageFromFile,
  blobToDataURL,
} from "../lib/processor";
import { saveAs } from "file-saver";
import DropZone from "./DropZone";
import TexturePreview from "./TexturePreview";
import "./SingleMode.css";

interface ProcessingResult {
  originalUrl: string;
  textureUrl: string;
  blob: Blob;
  fileName: string;
}

export default function SingleMode() {
  const [result, setResult] = useState<ProcessingResult | null>(null);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [shrinkFactor, setShrinkFactor] = useState(0.85);
  const [vignetteStrength, setVignetteStrength] = useState(0.3);
  const [quantize, setQuantize] = useState(true);
  const originalUrlRef = useRef<string | null>(null);

  const handleFile = useCallback(
    async (file: File) => {
      setError(null);
      setProcessing(true);
      setResult(null);

      if (originalUrlRef.current) {
        URL.revokeObjectURL(originalUrlRef.current);
      }

      try {
        const originalUrl = URL.createObjectURL(file);
        originalUrlRef.current = originalUrl;

        const img = await loadImageFromFile(file);
        const blob = await processImageToTexture(img, {
          shrinkFactor,
          vignetteStrength,
          quantize,
        });
        const textureUrl = await blobToDataURL(blob);
        const baseName = file.name.replace(/\.[^.]+$/, "");

        setResult({
          originalUrl,
          textureUrl,
          blob,
          fileName: `${baseName}_16x16.png`,
        });
      } catch (e) {
        setError(e instanceof Error ? e.message : "Processing failed");
      } finally {
        setProcessing(false);
      }
    },
    [shrinkFactor, vignetteStrength, quantize]
  );

  const handleDownload = () => {
    if (result) {
      saveAs(result.blob, result.fileName);
    }
  };

  const handleReprocess = async () => {
    if (!originalUrlRef.current || !result) return;
    setProcessing(true);
    setError(null);
    try {
      const img = new Image();
      await new Promise<void>((resolve, reject) => {
        img.onload = () => resolve();
        img.onerror = reject;
        img.src = result.originalUrl;
      });
      const blob = await processImageToTexture(img, {
        shrinkFactor,
        vignetteStrength,
        quantize,
      });
      const textureUrl = await blobToDataURL(blob);
      setResult((prev) =>
        prev ? { ...prev, textureUrl, blob } : null
      );
    } catch (e) {
      setError(e instanceof Error ? e.message : "Reprocessing failed");
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="single-mode">
      <div className="mode-header">
        <h2>Single Texture</h2>
        <p className="mode-desc">
          Upload a photo and convert it to a Minecraft-ready 16×16 pixel
          texture
        </p>
      </div>

      <div className="single-layout">
        <div className="single-left">
          <DropZone onFile={handleFile} disabled={processing} />

          <div className="settings-card">
            <h3>Processing Settings</h3>
            <div className="setting-row">
              <label>Shrink factor</label>
              <div className="setting-control">
                <input
                  type="range"
                  min="0.5"
                  max="1"
                  step="0.05"
                  value={shrinkFactor}
                  onChange={(e) => setShrinkFactor(Number(e.target.value))}
                />
                <span className="setting-value">
                  {Math.round(shrinkFactor * 100)}%
                </span>
              </div>
            </div>
            <div className="setting-row">
              <label>Vignette</label>
              <div className="setting-control">
                <input
                  type="range"
                  min="0"
                  max="0.8"
                  step="0.05"
                  value={vignetteStrength}
                  onChange={(e) =>
                    setVignetteStrength(Number(e.target.value))
                  }
                />
                <span className="setting-value">
                  {Math.round(vignetteStrength * 100)}%
                </span>
              </div>
            </div>
            <div className="setting-row">
              <label>Minecraft palette</label>
              <div className="setting-control">
                <label className="toggle">
                  <input
                    type="checkbox"
                    checked={quantize}
                    onChange={(e) => setQuantize(e.target.checked)}
                  />
                  <span className="toggle-slider" />
                </label>
              </div>
            </div>
            {result && (
              <button
                className="btn-secondary"
                onClick={handleReprocess}
                disabled={processing}
              >
                {processing ? "Processing…" : "Reapply settings"}
              </button>
            )}
          </div>
        </div>

        <div className="single-right">
          {error && (
            <div className="error-box">
              <span>⚠ {error}</span>
            </div>
          )}

          {processing && !result && (
            <div className="processing-state">
              <div className="spinner" />
              <p>Processing image…</p>
            </div>
          )}

          {result && (
            <div className="preview-section">
              <TexturePreview
                originalUrl={result.originalUrl}
                textureUrl={result.textureUrl}
              />
              <button
                className="btn-primary download-btn"
                onClick={handleDownload}
              >
                ↓ Download {result.fileName}
              </button>
            </div>
          )}

          {!result && !processing && !error && (
            <div className="empty-state">
              <div className="empty-icon">🖼</div>
              <p>Upload an image to get started</p>
              <p className="empty-sub">
                Supports PNG, JPG, WebP, and GIF
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
