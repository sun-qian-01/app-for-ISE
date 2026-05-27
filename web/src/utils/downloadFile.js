function parseFileName(contentDisposition) {
  if (!contentDisposition) {
    return "";
  }

  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8Match?.[1]) {
    try {
      return decodeURIComponent(utf8Match[1].trim());
    } catch {
      return utf8Match[1].trim();
    }
  }

  const plainMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
  return plainMatch?.[1]?.trim() || "";
}

function fallbackFileName(url, fallbackName) {
  if (fallbackName) {
    return fallbackName;
  }
  const raw = url.split("/").pop() || "download-file";
  return raw.split("?")[0] || "download-file";
}

export async function downloadWithAuth(url, fallbackName = "") {
  if (!url) {
    throw new Error("下载地址为空");
  }

  const token = localStorage.getItem("ise_token");
  const headers = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(url, {
    method: "GET",
    headers,
    credentials: "same-origin",
  });

  if (!response.ok) {
    throw new Error(`下载失败（HTTP ${response.status}）`);
  }

  const contentType = response.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    const payload = await response.json().catch(() => null);
    throw new Error(payload?.message || "下载失败");
  }

  const fileName =
    parseFileName(response.headers.get("content-disposition")) || fallbackFileName(url, fallbackName);
  const blob = await response.blob();
  const objectUrl = URL.createObjectURL(blob);

  try {
    const anchor = document.createElement("a");
    anchor.href = objectUrl;
    anchor.download = fileName;
    anchor.style.display = "none";
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
  } finally {
    URL.revokeObjectURL(objectUrl);
  }
}
