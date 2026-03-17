import { useState, useCallback, useRef } from "react";
import "./DropZone.css";

interface DropZoneProps {
  onFile: (file: File) => void;
  disabled?: boolean;
  label?: string;
  compact?: boolean;
}

const ACCEPTED = ["image/png", "image/jpeg", "image/webp", "image/gif"];

export default function DropZone({
  onFile,
  disabled,
  label = "Drop an image here or click to browse",
  compact = false,
}: DropZoneProps) {
  const [dragOver, setDragOver] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const accept = (file: File) => {
    if (!ACCEPTED.includes(file.type)) {
      alert("Please upload a PNG, JPG, WebP or GIF image.");
      return;
    }
    onFile(file);
  };

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      setDragOver(false);
      if (disabled) return;
      const file = e.dataTransfer.files[0];
      if (file) accept(file);
    },
    [disabled]
  );

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) accept(file);
    e.target.value = "";
  };

  return (
    <div
      className={`dropzone${dragOver ? " drag-over" : ""}${disabled ? " disabled" : ""}${compact ? " compact" : ""}`}
      onDragOver={(e) => {
        e.preventDefault();
        if (!disabled) setDragOver(true);
      }}
      onDragLeave={() => setDragOver(false)}
      onDrop={handleDrop}
      onClick={() => !disabled && inputRef.current?.click()}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => e.key === "Enter" && inputRef.current?.click()}
    >
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        style={{ display: "none" }}
        onChange={handleChange}
        disabled={disabled}
      />
      <span className="dropzone-icon">{compact ? "+" : "📂"}</span>
      <span className="dropzone-label">{label}</span>
    </div>
  );
}
