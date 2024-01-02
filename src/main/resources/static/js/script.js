console.log("this is script file")

const toggleSidebar=()=> {
    
    if($(".sidebar").is(":visible")){
        //true
        //band karna h

        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");
    } else{
        //false
        //show karna h
        
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");
    }



};
const search=()=>{
   // console.log("searchig....")
   let query=$("#search-input").val();

   if(query==''){
        $(".search-result").hide();
   }
   else{
    console.log(query);

    //sedind request to server
    let url=`http://localhost:8070/search/${query}`;
    fetch(url)
    .then((response)=>{
return response.json();
})
.then((data)=>{
	console.log(data);
	
	 let text=`<div class='list-group'>`;
    data.array.forEach((contact) => {
        text +=`<a href='#' class='list-group-item list-group-action'>${contact.name}</a>`;
    });
    text+=`</div>`;
    $(".search-result").html(text);
    $(".search-result").show();
   
    });
  
   }

};