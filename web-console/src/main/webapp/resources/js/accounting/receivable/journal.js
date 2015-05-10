
var JOURNALS = [
  {order: 'SA100001', qtyOfBox: '2', paidAmt: '1030.29'},
  {order: 'SA100021', qtyOfBox: '3', paidAmt: '1060.29'},
  {order: 'SA100051', qtyOfBox: '4', paidAmt: '1090.29'}
]

var IntlMixin = ReactIntl.IntlMixin;
var FormattedMessage = ReactIntl.FormattedMessage;

var JournalRow = React.createClass({
  render: function() {
    return ( 
      <tr>
        <td>{this.props.journal.order.orderNo}</td> <td>{this.props.journal.qtyOfBox}</td> <td>{this.props.journal.revenue}</td> 
      </tr>);
  }
})

var JournalsTableBody = React.createClass({
  mixins: [IntlMixin],

  getInitialState: function() {
    return {data: []};
  },

  componentDidMount: function() {
    $.get(this.props.source, function(result) {
      if (this.isMounted()) {
        this.setState({
          data: result
        });
      }
    }.bind(this));
  },

  render: function() {
    var rows = [];
    this.state.data.forEach(function(journal) {
      rows.push(<JournalRow journal={journal} />);
    }) 
    return (
      <table className="table table-striped table-hover table-condensed table-bordered table-16">
        <thead>
          <tr>
            <th><FormattedMessage message={this.getIntlMessage('order')} /></th>
            <th><FormattedMessage message={this.getIntlMessage('qty_of_box')} /></th>
            <th><FormattedMessage message={this.getIntlMessage('revenue')} /></th>
          </tr>
        </thead>

        <tbody>{rows}</tbody>
      </table>
    );
  }
})

React.render(
  <JournalsTableBody source="/api/receivable/journal?orderId=95" {...i18n} />,
  document.getElementById('journals')
);
