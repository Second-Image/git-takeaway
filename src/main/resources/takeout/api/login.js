function loginApi(data) {
    return $axios({
        'url': '/manForTakeaway/login',
        'method': 'post',
        data
    })
}
function addManForTakeaway (params) {
    return $axios({
        url: '/manForTakeaway/add',
        method: 'post',
        data: { ...params }
    })
}
function logoutApi(){
    return $axios({
        'url': '/manForTakeaway/logout',
        'method': 'post',
    })
}
