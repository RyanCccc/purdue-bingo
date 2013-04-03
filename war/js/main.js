var isModalOn = false;


function createEvent(restaurant,hour,min,eventname){
	var br='<hr>';
	if($("*[value="+restaurant+"]").next().children('.event').length === 0){
		br='';
	}
	var newEvent=$(br+'<div class="row-fluid event" id=1><div class="span2 headDiv"><img src="'+ userImg +'" class="img-circle head"></div><div class="span2 orgDiv"><div class="label label-info">Organizer</div><div class="display">'+fullname+'</div></div><div class="span2 eNameDiv"><div class="label label-info">Event Name</div><div class="display">'+eventname+'</div></div><div class="span2 timeDiv"><div class="label label-info">Time</div><br><div class="hourNum">'+hour+'</div> : <div class="minNum">'+min+'</div></div><div class="span2 countDiv"><div class="label label-info">Attendants</div><div class="display">0</div></div><div class="span2 joinDiv"><button type="submit" class="btn btn-info join" onclick="invite(this)">Invite!</button></div>');
	//newEvent.hide();
	$("*[value="+restaurant+"]").next().each(function(){
		$(this).append(newEvent.clone(true));
	});
	//alert('success');
	//newEvent.show(1000);
}

// function testClick(){
// 	createEvent('happy-china',11,10);
// }
function setCurrentDate(){
	$( "#datepicker" ).datepicker();
	var d=new Date();
	var month=d.getMonth()+1;
	var day=d.getDate();
	var year=d.getFullYear();
	if(month<10)
		month='0'+month.toString();
	if(day<10)
		day='0'+day.toString();
	year=year.toString();
	var date=month+'/'+day+'/'+year;
	$( "#datepicker" ).val(date);
}

$(function() {
	$(".error").css("visibility","hidden");
	$(".msg").hide();
	$('.timepick').timepicker({
		altField: '.timepick',
		defaultTime: '12:00'
	});

	setCurrentDate();

	$('#cattabs a').click(function (e) {
		e.preventDefault();
		$(this).tab('show');
	});

	$('.accordion').accordion({
		header: ".restaurant-header",
		collapsible: true,
		heightStyle: "content",
		active: false,
		beforeActivate: function(event, ui) {
			if(isModalOn) {
				event.preventDefault();
			}
		}
	});

	$('a[data-toggle="modal"]').click(function(obj){
		$("#create").data("restaurant",$(this).parent().attr('value'));
		isModalOn = true;
	});

	$('#new-event-modal').on('hidden', function() {
		isModalOn = false;
	});

	$("#create").click(function(){
		var name = $("#event-name").val();
		var restaurant=$(this).data("restaurant");
		var time = $(".timepick").timepicker('getTime');
		var hourandmin = time.split(":");
		var d = new Date();
		d.setHours(hourandmin[0]);
		d.setMinutes(hourandmin[1]);
		d.setSeconds(0);
		d.setMilliseconds(0);
		var date = d.getTime();

		var invites = $("#event-invite").val().split(/[\s,]+/);
		console.log('invites', invites);

		if (name === '') {
			$("#event-name").css("border", "1px solid #C83135");
			$(".error").css("visibility", "visible");
		} else {
			$('#event-name').css('border', '');
			$('.error').css('visibility', 'hidden');

			var url = "/event?";
			url += "action=add";
			url += "&name=" + name;
			url += "&restaurant=" + restaurant;
			url += "&date=" + date;

			if (typeof username !== 'undefined') {
				url += '&username=' + username;
			}

			if ($("#event-invite").val() !== '') {
				for (var i = 0;  i < invites.length; i++) {
					if (invites[i] !== '') {
						url += "&invite=" + invites[i];
					}
				}
			}

			console.log('create url', url);

			$.getJSON(url, function(data) {
				var status = data['status'];

				if (status === 0) {
					// succeed
					createEvent(restaurant,hourandmin[0],hourandmin[1],name);
					$('#new-event-modal').modal('hide');
				} else {
					// failed
					var reason = data['reason'];
					console.log('create failed', reason);
				}
			})
			.error(function() {
				// Error
				console.log('create request failed.');
			});
		}
	});

});

function join(obj) {
	var restaurant = $(obj).parent().parent().parent().prev().attr('value');
	var eventID = $(obj).parent().parent().attr('eventid');

	var url = "/event?";
		url += "action=join";
		url += "&id=" + eventID;
		url += '&restaurant=' + restaurant;
		url += "&username=" + username;

	console.log('join url', url);

	$.getJSON(url, function(data) {
		var status = data['status'];

		if (status === 0) {
			// succeed
			console.log('join success');
			successPrompt('Join success!');
			$(obj).addClass('disabled');
		} else {
			// failed
			var reason = data['reason'];
			console.log('join failed', reason);
		}
	})
	.error(function() {
		// Error
		console.log('join request failed.');
	});
}

function successPrompt(msg){
	$('#msg').text(msg);
	var top=$(window).height()/2-$('.msg').height()/2;
	var left=$(window).width()/2-$('.msg').width()/2;
	$('.msg').css('left',left);
	$('.msg').css('top',top);
	setTimeout(function(){
		$('.msg').show("bounce",1000);
	},100);
	setTimeout(function(){
		$('.msg').hide('fade',1000);
	},500);
}

function invite(obj) {
	$('#new-invite-modal').modal('show');
	var restaurant = $(obj).parent().parent().parent().prev().attr('value');
	var eventID = $(obj).parent().parent().attr('eventid');
	$('#inviteBtn').click(function() {
		var invites=$('#inviteContent').val().split(',');

		var url = "/event?";
			url += "action=invite";
			url += "&id=" + eventID;
			url += "&restaurant=" + restaurant;

			if ($("#inviteContent").val() !== '') {
				for (var i = 0;  i < invites.length; i++) {
					if (invites[i] !== '') {
						url += "&invite=" + invites[i];
					}
				}
			}

		console.log('invite url', url);

		//TODO AJAX TO CONNECT SERVER
		$.getJSON(url, function(data) {
			var status = data['status'];

			if (status === 0) {
				// succeed
				$('#new-invite-modal').modal('hide');
				setTimeout(function(){
					successPrompt('Invite success!');
				},1000);
			} else {
				// failed
				var reason = data['reason'];
				console.log('invite failed', reason);	
			}
		})
		.error(function() {
			// Error
			console.log('invite request failed.');
		});
	});
}