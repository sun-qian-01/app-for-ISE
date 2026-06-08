九、GPT模型代码中调用
若要在代码中使用，例如 HTTP 请求或者官方的 SDK，那么：
- 请求地址：https://gmn.chuangzuoli.com/v1/responses
- 请求方法：POST
- 请求头
  - Content-Type：application/json
  - Authorization：Bearer sk-xxxx
- 请求体格式需要注意一下，其中 input 需要使用数组方式，下面给出一个最小可用 demo（以及其他可选参数，这里没有列举出来，可以自己抓包调试一下）
{
  "model": "gpt-5.4",
  "input": [
    {
      "type": "message",
      "role": "developer",
      "content": [
        {
          "type": "input_text",
          "text": "123"
        }
      ]
    },
    {
      "type": "message",
      "role": "user",
      "content": [
        {
          "type": "input_text",
          "text": "456"
        }
      ]
    }
  ]
}

valid api: sk-c324e1b52a086c8b595f7ba1f290683b4da37a2b75faaf779d4a674f9012c2c3
