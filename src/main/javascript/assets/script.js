console.clear();

var w = 960,
    h = 700;

//Specify a color scale
var color = d3.scale.category20();
	
var svg = d3.select('body').append('svg')
  .attr('width', w)
  .attr('height', h);

  //Create tooltips to display on hover
	var tooltip = d3.select("body")
		.append("div")
		.attr("class", "tooltip")
		.style("visibility", "hidden");
		
		/*.style("position", "relative")
		.style("z-index", "10")
		.style("visibility", "hidden")
		.style("color", "white")
		.style("padding", "8px")
		.style("background-color", "rgba(0, 0, 0, 0.75)")
		.style("border-radius", "6px")
		.style("font", "12px sans-serif")
		.text("tooltip");*/
  
d3.json('files/www_nytimes_comResourcesTimes.json', function(err, json) {
  
  
  var force = d3.layout.force()
    .size([w,h])
    .charge(-100)
	.linkDistance(300)
    .on('start', start)
    .nodes(json.nodes)
    .links(json.links)
    .start();
  
  var links = svg.selectAll('.link')
    .data(json.links)
    .enter().append('line')
    .attr('class', 'link')
	.on('dblclick.tooltip', function(d) {
       	tooltip.style("visibility", "visible");
      	tooltip.html("Percentile5: "+ d.percentileFive + "ms " + 
                     "<p/>Median: " + d.median + "ms " +
                    "<p/>Percentile95: "  + d.percentileNinetyFive + "ms ")
        	.style("left", (d3.event.pageX) + "px")
        	.style("top", (d3.event.pageY + 10) + "px");
    	})
	.style("stroke-width", function (d) {
			return 1;
		});
	
	
	/*.on("dblclick", function(d) { 
				//tooltip.text(d.value);
				tooltip.style("visibility", "visible");
				tooltip.html(()=>{
					let innerTableContent = "<tr>"+
                            "<th scope='row'>Percentile 5 between nodes :</th>"+ "<td>"+d.percentileFive+"ms &#013</td>"+
							"<th scope='row'>Median between nodes :</th>"+ "<td>"+d.median+"ms &#013</td>"+
							"<th scope='row'>Percentile 95 between nodes :</th>"+ "<td>"+d.percentileNinetyFive+"ms</td>"+
                            "</tr>";
					return "<div class='card bg-dark'>"+"<div class='card-body'>"+
                        "<table class='table table-striped table-dark'>"+
                        "<tbody>"+
                        innerTableContent +
                        "</tbody>" +
                        "</table>"+
                        "</div></div>";
				}).style("left", (d3.event.pageX) + "px")
				.style("top", (d3.event.pageY) + "px");
			})
	.style("stroke-width", function (d) {
				return 1;
			});*/
  
  var nodes = svg.selectAll('.node')
    .data(json.nodes)
    .enter().append('circle')
    .attr('class', 'node')
	//.style("fill", function (d) {
	//			return color(d.group);
	//		})
	.style("fill", function (d) {
		if(d.type == "XHR")
			return color(d.group);
	})
    .attr('r', 5)
    .call(force.drag)
	.on("mouseover", function(d) { 
			tooltip.style("visibility", "visible");
				tooltip.html("Name: "+ d.name + 
							 "<p/>Type: " + d.type +
							"<p/>Probability: "  + d.probability + "% " + 
							"<p/>Cacheable: "  + d.cacheable)
					.style("left", (d3.event.pageX) + "px")
					.style("top", (d3.event.pageY + 10) + "px");
				})
	
	  .on("mouseout",  function() { 
	  	return tooltip.style("visibility", "hidden");
	  })
	  .on('dblclick', connectedNodes);  //Implements focus on double-clicked node's network (connectedNodes function)
      
  function start() {
    var ticksPerRender = 3;
    requestAnimationFrame(function render() {
      for (var i = 0; i < ticksPerRender; i++) {
        force.tick();
      }
      links
        .attr('x1', function(d) { return d.source.x; })
        .attr('y1', function(d) { return d.source.y; })
        .attr('x2', function(d) { return d.target.x; })
        .attr('y2', function(d) { return d.target.y; });
      nodes
        .attr('cx', function(d) { return d.x; })
        .attr('cy', function(d) { return d.y; });
      
      if (force.alpha() > 0) {
        requestAnimationFrame(render);
      }
    })
  }
  
  /*The next code block makes it so that double-clicking shows only the clicked node's network*/
		//Toggle stores whether a node has been double-clicked
		var toggle = 0;
		//Create an array to log which nodes are connected to which other nodes
		var linkedByIndex = {};
		for (i = 0; i < json.nodes.length; i++) {
			linkedByIndex[i + "," + i] = 1;
		};
		json.links.forEach(function (d) {
			linkedByIndex[d.source.index + "," + d.target.index] = 1;
		});
		//This function looks up whether a pair are neighbors
		function neighboring(a, b) {
			return linkedByIndex[a.index + "," + b.index];
		}
		function connectedNodes() {
			if (toggle == 0) {
				//Reduce the opacity of all but the neighboring nodes
				d = d3.select(this).node().__data__;
				nodes.style("opacity", function (o) {
					return neighboring(d, o) | neighboring(o, d) ? 1 : 0.1;
				});
				nodes.style("fill", function (o) {
					if (neighboring(d, o) | neighboring(o, d))
						return color(o.group);
				});
				links.style("opacity", function (o) {
					return d.index==o.source.index | d.index==o.target.index ? 1 : 0.1;
				});
				links.style("stroke", function (o) {
					if (d.index==o.source.index | d.index==o.target.index){
						return "#5882FA";
					}
				});
				toggle = 1;
			} else {
				//Put them back to opacity=1
				nodes.style("opacity", 1);
				nodes.style("fill", function (d) {
					if(d.type == "XHR")
						return color(d.group);
			});
				links.style("opacity", 1);
				links.style("stroke", "#ccc");
				toggle = 0;
			}
		}
  
});