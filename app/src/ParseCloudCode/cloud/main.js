
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