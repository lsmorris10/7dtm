import "./TexturePreview.css";

interface TexturePreviewProps {
  originalUrl: string;
  textureUrl: string;
}

export default function TexturePreview({
  originalUrl,
  textureUrl,
}: TexturePreviewProps) {
  return (
    <div className="texture-preview">
      <div className="preview-pane">
        <div className="preview-label">Original</div>
        <div className="preview-img-wrap">
          <img src={originalUrl} alt="Original" className="preview-img-original" />
        </div>
      </div>
      <div className="preview-arrow">→</div>
      <div className="preview-pane">
        <div className="preview-label">Minecraft Texture (16×16)</div>
        <div className="preview-img-wrap">
          <img
            src={textureUrl}
            alt="Processed 16x16 texture"
            className="preview-img-texture"
            style={{ imageRendering: "pixelated" }}
          />
        </div>
        <div className="texture-actual">
          <img
            src={textureUrl}
            alt="Actual size"
            width={16}
            height={16}
            style={{ imageRendering: "pixelated" }}
          />
          <span className="actual-label">Actual size</span>
        </div>
      </div>
    </div>
  );
}
