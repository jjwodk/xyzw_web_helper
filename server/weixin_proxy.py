"""
微信二维码代理服务
解决 APK/WebView 中 CORS 跨域问题
"""

from flask import Flask, request, Response
from flask_cors import CORS
import requests

app = Flask(__name__)
CORS(app)  # 允许跨域


@app.route('/api/weixin/qrcode', methods=['GET'])
def get_qrcode():
    """
    获取微信登录二维码
    请求微信登录页面 HTML，从中提取二维码 URL
    """
    # 微信二维码 API URL
    weixin_url = (
        "https://open.weixin.qq.com/connect/app/qrconnect"
        "?appid=wxfb0d5667e5cb1c44"
        "&bundleid=com.hortor.games.xyzw"
        "&scope=snsapi_base,snsapi_userinfo,snsapi_friend,snsapi_message"
        "&state=weixin"
    )

    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Linux; Android 10; Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/83.0.4103.106 Mobile Safari/537.36 MicroMessenger/7.0.20.466(0x26070135) Process/tools0 NetType/WIFI Language/zh_CN',
            'Referer': 'https://open.weixin.qq.com/',
        }

        # 请求微信登录页面（返回 HTML）
        resp = requests.get(weixin_url, headers=headers, timeout=10)
        
        # 直接返回 HTML 内容，前端会解析其中的二维码 URL
        return Response(resp.text, content_type='text/html; charset=utf-8')
    except Exception as e:
        return {'error': str(e)}, 500


@app.route('/api/weixin/poll', methods=['GET'])
def poll_qrcode():
    """
    轮询二维码扫描状态
    前端传入 uuid 参数
    """
    uuid = request.args.get('uuid', '')
    
    if not uuid:
        return {'error': '缺少 uuid 参数'}, 400

    # 微信轮询 URL
    weixin_url = f"https://open.weixin.qq.com/connect/l/qrconnect?uuid={uuid}&f=url&_={request.args.get('_', '')}"

    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Linux; Android 10; Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/83.0.4103.106 Mobile Safari/537.36 MicroMessenger/7.0.20.466(0x26070135) Process/tools0 NetType/WIFI Language/zh_CN',
            'Referer': 'https://open.weixin.qq.com/',
            'Accept': '*/*',
        }

        resp = requests.get(weixin_url, headers=headers, timeout=10)
        
        # 返回 JavaScript 代码，前端检查 window.wx_errcode
        return Response(resp.text, content_type='application/javascript; charset=utf-8')
    except Exception as e:
        return {'error': str(e)}, 500


@app.route('/api/hortor/login', methods=['POST'])
def hortor_login():
    """
    仙境传说登录代理
    转发查询参数和请求体到目标服务器
    """
    # 基础 URL
    base_url = "https://comb-platform.hortorgames.com/comb-login-server/api/v1/login"
    
    # 获取查询参数（gameId, timestamp, version 等）
    query_params = request.args.to_dict()
    
    # 构建带参数的 URL
    if query_params:
        from urllib.parse import urlencode
        login_url = f"{base_url}?{urlencode(query_params)}"
    else:
        login_url = base_url

    try:
        # 获取前端发送的数据（text/plain 格式）
        data = request.data
        
        # 打印调试信息
        print(f"[代理] 请求 URL: {login_url}")
        print(f"[代理] 请求体长度: {len(data)} bytes")
        
        headers = {
            'User-Agent': 'Mozilla/5.0 (Linux; Android 12; 23117RK66C Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/95.0.4638.74 Mobile Safari/537.36',
            'Accept': '*/*',
            'Content-Type': 'text/plain; charset=utf-8',
            'Origin': 'https://open.weixin.qq.com',
            'Referer': 'https://open.weixin.qq.com/',
            'Host': 'comb-platform.hortorgames.com',
            'Connection': 'keep-alive',
        }

        resp = requests.post(
            login_url,
            data=data,
            headers=headers,
            timeout=10
        )

        print(f"[代理] 响应状态: {resp.status_code}")
        print(f"[代理] 响应内容: {resp.text[:500] if resp.text else 'empty'}")

        return Response(resp.content, content_type='application/json')
    except Exception as e:
        print(f"[代理] 错误: {str(e)}")
        return {'error': str(e)}, 500


if __name__ == '__main__':
    # 运行在 3000 端口
    app.run(host='0.0.0.0', port=3000, debug=True)
