/* Global variables */
var orderId = getParameterByName('orderId');  

var definition = {
  orderId: orderId,
  source: '/api/receivable/journal?orderId=' + orderId,
  columns: [
    {name: 'id', type: 'hidden'},
    {name: 'order', type: 'descendant', pattern: 'order.orderNo'},
    {name: 'qtyOfBox'},
    {name: 'revenue'},
    {name: 'recognizedAt', type: 'date', pattern: 'YYYY-MM-DD'},
    {name: 'goods', type: 'descendant', pattern: 'order.goods.name'},
    {name: 'receivableOrg', type: 'descendant', pattern: 'order.customer.name'},
    {name: 'receivedAmt'},
    {name: 'receivedAt', type: 'date', pattern: 'YYYY-MM-DD'},
    {name: 'invoiceNo'},
    {name: 'invoiceTo', type: 'descendant', pattern: 'order.customer.name'},
    {name: 'invoicedAmt'},
    {name: 'invoicedAt', type: 'date', pattern: 'YYYY-MM-DD'}
  ],
  modal: {
    title: 'crj',
    fields: [
      {name: 'id', type: 'hidden'},
      {name: 'order', type: 'hidden', value: 'descendant', pattern: 'order.id', default: orderId},
      {name: 'qtyOfBox'},
      {name: 'revenue'},
      {name: 'recognizedAt', type: 'date', pattern: 'YYYY-MM-DD mm:ss'},
      {name: 'receivedAmt'},
      {name: 'receivedAt', type: 'date', pattern: 'YYYY-MM-DD mm:ss'},
      {name: 'invoiceNo'},
      {name: 'invoicedAmt'},
      {name: 'invoicedAt', type: 'date', pattern: 'YYYY-MM-DD mm:ss'}
    ]
  }
}

React.render(
  <CrudTable definition={definition} {...i18n} />,
  document.getElementById('crudTable')
);
