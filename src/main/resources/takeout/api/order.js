// 查询列表页接口
const getOrderDetailPage = (params) => {
    return $axios({
        url: '/manForTakeaway/order/page',
        method: 'get',
        params
    })
}

// 查看接口 //订单内容打印出来给商家
// const queryOrderDetailById = (id) => {
//   return $axios({
//     url: `/orderDetail/${id}`,
//     method: 'get'
//   })
// }

// 取消，派送，完成接口
const editOrderDetail = (params) => {
    return $axios({
        url: '/manForTakeaway/order',
        method: 'put',
        data: { ...params }
    })
}
