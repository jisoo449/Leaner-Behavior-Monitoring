// Set new default font family and font color to mimic Bootstrap's default styling
Chart.defaults.global.defaultFontFamily = 'Nunito', '-apple-system,system-ui,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif';
Chart.defaults.global.defaultFontColor = '#858796';

$(document).ready(function(){
    $.ajax({         
        type:"GET",
        url:"http://localhost:8080/dashboard/board/getDetailDetail",
        dataType:"JSON",
        success : function(obj) {
            getEmotioncallback(obj);            
        },           
        error:function(request,status,error){
            alert("도넛차트 : "+"code:"+request.status+"\n"+"message:"+request.responseText+"\n"+"error:"+error);
        }
            
    });
});

function getEmotioncallback(obj){

    var list = obj;
    var listLen = obj.length;
		
    console.log(list);
    console.log(listLen);
    
    var emotionLabel=new Array();
    var emotionAmount=new Array();
    var emotionColor=new Array();
    var tmp=0;
    var idx=-1;
    var pre=-1;

    if(listLen >  0){
        
        for(var a=0;a<listLen;a++){
            if(pre==list[a].emotion){
                emotionAmount[idx]=++tmp;
            }
            else{
            	pre=list[a].emotion;
                idx++;
                tmp=1;
                emotionAmount[idx]=tmp;
                switch(list[a].emotion){
                    case 0:
                        emotionLabel[idx]="분노";
                        emotionColor[idx]='#E77474';
                        break;
                    case 1:
                        emotionLabel[idx]="역겨움";
                        emotionColor[idx]='#50607E';
                        break;
                    case 2:
                        emotionLabel[idx]="공포";
                        emotionColor[idx]='#8268B1';
                        break;
                    case 3:
                        emotionLabel[idx]="행복";
                        emotionColor[idx]='#FFDFA1';
                        break; 
                    case 4:
                        emotionLabel[idx]="슬픔";
                        emotionColor[idx]='#96DFD7';
                        break;
                    case 5:
                        emotionLabel[idx]="놀람";
                        emotionColor[idx]='#EAB09E';
                        break;
                    case 6:
                        emotionLabel[idx]="무표정";
                        emotionColor[idx]='#B4B4B4';
                        break;
                    case 7:
                    emotionLabel[idx]="인식 안됨";
                    emotionColor[idx]='#3E3E3E';
                    break;                                           
                }

            }

        }

        for(var a=0;a<=idx;a++){
            emotionAmount[a]=emotionAmount[a]*100/listLen;
        }
    }
	
	var ctx = document.getElementById("myPieChart");
	var myPieChart = new Chart(ctx, {
	  type: 'doughnut',
	  data: {
	    labels: emotionLabel,
	    datasets: [{
	      data: emotionAmount,
	      backgroundColor: emotionColor
	    }],
	  },
	  options: {
	    maintainAspectRatio: false,
	    tooltips: {
	      backgroundColor: "rgb(255,255,255)",
	      bodyFontColor: "#858796",
	      borderColor: '#dddfeb',
	      borderWidth: 1,
	      xPadding: 15,
	      yPadding: 15,
	      displayColors: false,
	      caretPadding: 10,
	    },
	    legend: {
	      display: false
	    },
	    cutoutPercentage: 80,
	  },
	});
}
