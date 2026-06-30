export function parseSseEvents(chunk) {
  return chunk
    .split(/\n\n+/)
    .map((block) => block.trim())
    .filter(Boolean)
    .map(parseSseBlock)
    .filter(Boolean);
}

function parseSseBlock(block) {
  let event = "message";
  const dataLines = [];
  for (const line of block.split(/\n/)) {
    if (line.startsWith("event:")) {
      event = line.slice("event:".length).trim() || "message";
    } else if (line.startsWith("data:")) {
      dataLines.push(line.slice("data:".length).trimStart());
    }
  }
  if (!dataLines.length) {
    return null;
  }
  const rawData = dataLines.join("\n");
  let data = rawData;
  try {
    data = JSON.parse(rawData);
  } catch {}
  return { event, data };
}
