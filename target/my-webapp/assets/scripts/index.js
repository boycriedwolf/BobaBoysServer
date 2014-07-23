App = Ember.Application.create();

App.Router.map(function() {
  // put your routes here
  this.resource("menu");
});

App.MenuRoute = Ember.Route.extend({
    model: function(){
        return menuItems;
    }
})

var menuItems = [{
    id:"1",
    name: "Pearl Milk Tea",
    descrip: "The reason why you came to us.",
    price:"$3.99"     
},{
    id:"2",
    name: "Minced Pork Rice",
    descrip: "Looks like dog food. Tastes like heaven.",
    price:"$4.99"         
},{
    id:"3",
    name: "Thick Toast",
    descrip: 'Yes, "Texas Toast" originated in Asia.',
    price:"$3.99"     
},{
    id:"4",
    name: "M. Pork Rice & P. Milk Tea",
    descrip: "Combo #1",
    price:"$6.99"     
},{
    id:"5",
    name: "T. Toast & P. Milk Tea",
    descrip: "Combo #2",
    price:"$5.99"     
}]
