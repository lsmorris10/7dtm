export interface AIGenerationResult {
  success: boolean;
  dataUrl?: string;
  error?: string;
}

export async function generateImageFromDescription(
  description: string
): Promise<AIGenerationResult> {
  try {
    const response = await fetch("/api/generate-image", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ prompt: description }),
    });

    if (!response.ok) {
      throw new Error(`Server returned ${response.status}`);
    }

    const data = await response.json();
    if (data.dataUrl) {
      return { success: true, dataUrl: data.dataUrl };
    }
    throw new Error("No image data returned");
  } catch {
    return {
      success: false,
      error:
        "AI image generation is not available. Please upload a photo instead.",
    };
  }
}
