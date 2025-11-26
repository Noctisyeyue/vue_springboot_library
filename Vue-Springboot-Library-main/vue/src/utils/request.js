import axios from 'axios'
import router from "../router";

const request = axios.create({
    baseURL: '/api',//给所有通过这个 request 实例发出的请求都加上了 /api 前缀
    timeout: 5000
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
// 比如统一加token，对请求参数统一加密
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';//统一设置请求头告诉后端"我发的是 JSON 数据"

    // config.headers['token'] = user.token;  // 设置请求头
    //取出sessionStorage里面缓存的用户信息
    let userJson = sessionStorage.getItem("user")  //检查浏览器里是否有登录用户信息
    if(!userJson)                           //如果没登录 → 强制跳到登录页
    {
        router.push("/login")
    }
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {                   //axios 把服务器返回的完整信息包装在了一个 response 对象里
        let res = response.data;   //axios 把真正的数据放在 response.data 里
        // 如果是返回的文件
        if (response.config.responseType === 'blob') {
            return res    //直接返回
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res  //转换成真正的 JavaScript 对象
        }
        return res;
    },
    error => {
        console.log('err' + error) // for debug
        return Promise.reject(error)
    }
)


export default request

