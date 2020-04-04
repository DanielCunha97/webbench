var ctx = document.getElementById('firstChart').getContext('2d');
        const xLabels = [];
        var chart = new Chart(ctx, {
            // The type of chart we want to create
            type: 'bar',
            // The data for our dataset
            data: {
                labels: xLabels,
                datasets: [{
                    label: null,
                    backgroundColor: 'rgb(0, 0, 0)',
                    borderColor: 'rgb(0, 0, 0)',
                    data: [0, 10, 5, 2, 20, 30, 45]
                }]
            },

            // Configuration options go here
            options: {
                title: {
                    display: true,
                    fontFamily:'Roboto',
                    text: ['Estimated total time per operator','Information on the total time for each operator'],
                    fontSize: 16,
                    
                }              
            }
        });

        getData('C:/Users/Daniel Cunha/Desktop/webbench/DataToCharts.csv');

        function getData(path){
            const reader = new FileReader(path);
            //reader.readAsBinaryString(path);
           // const table = path.split('\n').slice(0);
        //     console.log(reader);
        //    /* table.forEach( row => {
        //         const columns = row.split(',');
        //         const klmOperator = columns[0];
        //         xLabels.push(klmOperator);
        //         const times = columns[1];
        //         console.log(klmOperator,times);
        //     });*/
        //     reader.onload = function () {
        //         document.getElementById('csvData').innerHTML = reader.result;
        //     };
            // start reading the file. When it is done, calls the onload event defined above.
            //reader.readAsBinaryString(fileInput.files[0]);

            const fs = require('fs').remote;

            
  
            fs.readFile(path, (err, data) => { 
                if (err) throw err; 
            
                console.log(data.toString()); 
            }) 
        }
