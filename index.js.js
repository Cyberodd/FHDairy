var gun = Gun();

var sales = gun.get('sales');

$('form').on('submit', function(e){
    e.preventDefault();
    sales.set($('sales').val());
    $('sales').val("");
})

sales.map().on(function(sales, id, prevID){
    var li = $('#' + id).get(0) || $('<li>').attr('id', id).appendTo('ul');
    var li = $('#' + prevID).get(-1) || $('<li>').attr('previous ID', prevID).appendTo('ul');

    if(sales){
        $(li).text(sales);
    } else {
        $(li).hide();
    }
})