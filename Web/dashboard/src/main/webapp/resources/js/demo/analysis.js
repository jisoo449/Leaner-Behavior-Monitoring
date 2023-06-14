$(document).ready(function(){
    $.ajax({         
        type:"GET",
        url:"http://localhost:8080/dashboard/board/getDetail",
        dataType:"JSON",
        success : function(obj) {
            getDetailCallback(obj);        
        },           
        error:function(request,status,error){
            alert("code:"+request.status+"\n"+"message:"+request.responseText+"\n"+"error:"+error);
        }
            
    });
})

function getDetailCallback(obj){
    var list = obj;
    var listLen = obj.length;

    console.log(list);
    console.log(listLen);

	var str2="";
    var str="";
    var stremotion="";

    //라운드를 학생 테이블에서 가져와야 함

    if(listLen>0){
		
		var avg=0;
		
		for(var a=0;a<listLen;a++){
			avg+=list[a].score;
		}
		
		avg/=listLen;
		
		str2+="총점 : "+avg;
		
		
		var idx=0;
		
        //학습태도 평가
        var total_score = list[idx].score;//총점
        var total_time = list[idx].totalTime;//총 공부 시간
        var total_sleep = list[idx].blink;//총 졸음 시간
        var total_other = list[idx].gaze+list[idx].slope+list[idx].hand;//총 딴생각 시간
        var no_person 	= list[idx].e7;//자리에 없던 횟수
        var feedback = list[idx].feedback;
        
        str+="<p/>총점: "+total_score;
        str+="<p/>총 학습시간: "+total_time;
        str+="<p/>졸음 횟수: " + total_sleep; 
        str+="<p/>딴생각 :  " + total_other;
        str+="<p/>자리 이탈: " + no_person;
        
        
        
        //감정
        var max=0;
        var maxid=0;   
        if(list[idx].e0>list[idx].e1){
        	max=list[idx].e0;
        	maxid=0;
        }
        else{ 
        	max=list[idx].e1;
        	maxid = 1;
         }
         if(max<list[idx].e2){
         	max=list[idx].e2;
         	maxid=2;
         }
         if(max<list[idx].e3){
         	max=list[idx].e3;
         	maxid=3;
         }
         if(max<list[idx].e4){
         	max=list[idx].e4;
         	maxid=4;
         }
         if(max<list[idx].e5){
         	max=list[idx].e5;
         	maxid=5;
         }
         if(max<list[idx].e6){
         	max=list[idx].e6;
         	maxid=6;
         }
         if(max<list[idx].e7){
         	max=list[idx].e7;
         	maxid=7;
         }
         
         switch(maxid){
         	case 0:
         		stremotion+="분노"
         		break;
         	case 1:
         		stremotion+="역겨움"
         		break;
         	case 2:
         		stremotion+="공포"
         		break;
         	case 3:
         		stremotion+="행복"
         		break;
         	case 4:
         		stremotion+="슬픔"
         		break;
         	case 5:
         		stremotion+="놀람"
         		break;
         	case 6:
         		stremotion+="무표정"
         		break;
         	case 7:
         		stremotion+="인식 안됨"
         		break;
         }
         
        
            
    } else {
			str += "<p/>등록된 학습이 존재하지 않습니다.";
		}
		
	$("code").html(stremotion);
	$("#totalavg").html(str2);
	$(".study-analysis").html(str);
}