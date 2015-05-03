
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  	response.success("Hello world!");
});

Parse.Cloud.define("SendEmail", function(request,response) {

		console.log(request.params);

		var Mailgun = require('mailgun');
		Mailgun.initialize('sandbox4a8014d1bb164f24a96593b611a1f331.mailgun.org', 'key-dda3216e635e034f606c3d653f78557a');	

		Mailgun.sendEmail({
  		to: request.params["to"],
  		from: request.params["from"],
  		subject: request.params["name"]+" has named you his partner in doUI app!",
  		text: "To start sharing your daily tasks, download the App and sign up.\nWhen asked for your shared password enter:\n"+request.params["shared_password"]
	}, {
  		success: function(httpResponse) {
    		console.log(httpResponse);
    		response.success("Email sent!");
  		},
  		error: function(httpResponse) {
    		console.error(httpResponse);
   			response.error("Uh oh, something went wrong"+request);
  		}
	});
});

Parse.Cloud.define("SendPushCode", function(request,response) {

	console.log(request.params);

	var query = new Parse.Query(Parse.Installation);
	query.equalTo('user_id',request.params["user_id"]);

	var alertMessage = "";
	var push_code = request.params["push_code"];
	if (push_code == 100) {
		alertMessage = "PUSH_CODE_UPDATE_PARTNER";
	} else if (push_code == 200) {
		alertMessage = "PUSH_CODE_URGENT_TASK"
	} else if (push_code == 201) {
		alertMessage = "PUSH_CODE_NOTIFY_DONE"
	} else if (push_code == 300) {
		alertMessage = "PUSH_CODE_URGENT_SHOPPING"
	}



	Parse.Push.send({
		where: query,
		data: {
			alert: request.params["push_code"]+": "+alertMessage+" "+request.params["object_id"],
			push_code:request.params["push_code"],
			object_id:request.params["object_id"]
		}		
	}, {
		success: function(){
			console.log("sent succssfully");
			response.success("Push Sent!");
		},
		error: function(error) {
			console.log(error);
			response.error(error);
		}
	}
	);
});