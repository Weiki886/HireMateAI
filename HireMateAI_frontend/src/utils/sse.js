/**
 * 读取 fetch 返回的 SSE 流，按行解析 data: 后的 JSON（兼容 data: 与 data: 后无空格）。
 * 支持的事件: message, done, error
 */
export async function consumeSseJson(response, onEvent) {
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const parts = buffer.split('\n')
    buffer = parts.pop() ?? ''

    for (const raw of parts) {
      const line = raw.replace(/\r$/, '').trim()
      if (!line.startsWith('data:')) continue
      const jsonStr = line.slice(5).trimStart()
      if (!jsonStr) continue
      try {
        const data = JSON.parse(jsonStr)
        // 仅把字符串 content 当作正文（忽略 JSON null，避免前端误判）
        if (Object.prototype.hasOwnProperty.call(data, 'content') && typeof data.content === 'string') {
          onEvent({ type: 'message', data })
        } else if (data.error !== undefined) {
          onEvent({ type: 'error', data })
        } else {
          onEvent({ type: 'unknown', data })
        }
      } catch (e) {
        console.warn('SSE JSON 解析失败:', jsonStr.slice(0, 120), e)
      }
    }
  }

  const tail = buffer.replace(/\r$/, '').trim()
  if (tail.startsWith('data:')) {
    const jsonStr = tail.slice(5).trimStart()
    if (jsonStr) {
      try {
        const data = JSON.parse(jsonStr)
        if (Object.prototype.hasOwnProperty.call(data, 'content') && typeof data.content === 'string') {
          onEvent({ type: 'message', data })
        } else if (data.error !== undefined) {
          onEvent({ type: 'error', data })
        }
      } catch (_) { /* ignore */ }
    }
  }
}
