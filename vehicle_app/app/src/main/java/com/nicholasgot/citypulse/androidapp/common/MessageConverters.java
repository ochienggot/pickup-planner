package com.nicholasgot.citypulse.androidapp.common;

import citypulse.commons.contextual_filtering.contextual_event_request.ContextualEventRequest;
import citypulse.commons.contextual_filtering.contextual_event_request.Place;
import citypulse.commons.contextual_filtering.contextual_event_request.PlaceAdapter;
import citypulse.commons.reasoning_request.Answer;
import citypulse.commons.reasoning_request.Answers;
import citypulse.commons.reasoning_request.ReasoningRequest;
import citypulse.commons.reasoning_request.concrete.AnswerAdapter;
import citypulse.commons.reasoning_request.concrete.FunctionalConstraintValueAdapter;
import citypulse.commons.reasoning_request.concrete.FunctionalParameterValueAdapter;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalConstraintValue;
import citypulse.commons.reasoning_request.functional_requirements.FunctionalParameterValue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MessageConverters {

	public final static String contextualEventRequestToJSON(
			ContextualEventRequest contextualEventRequest) {

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Place.class, new PlaceAdapter());
		Gson gson = builder.create();

//		GsonBuilder wbuilder = new GsonBuilder();
//
//		wbuilder.registerTypeAdapter(FunctionalParameterValue.class,
//				new FunctionalParameterValueAdapter());
//		wbuilder.registerTypeAdapter(FunctionalConstraintValue.class,
//				new FunctionalConstraintValueAdapter());
//		wbuilder.registerTypeAdapter(Answer.class, new AnswerAdapter());

		return gson.toJson(contextualEventRequest);
	}

	public final static ContextualEventRequest contextualEventRequestFromJSON(
			String request) {

//		GsonBuilder wbuilder = new GsonBuilder();
//
//		wbuilder.registerTypeAdapter(FunctionalParameterValue.class,
//				new FunctionalParameterValueAdapter());
//		wbuilder.registerTypeAdapter(FunctionalConstraintValue.class,
//				new FunctionalConstraintValueAdapter());
//		wbuilder.registerTypeAdapter(Answer.class, new AnswerAdapter());

		
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Place.class, new PlaceAdapter());
		Gson gson = builder.create();
		
		return (ContextualEventRequest) gson.fromJson(request,
				ContextualEventRequest.class);

	}

	public final static Answers decisionSupportResponsefronJson(String response) {

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Answer.class, new AnswerAdapter());

		Gson gson = builder.create();

		return (Answers) gson.fromJson(response, Answers.class);

	}

	public final static String decisionSupportResponsetoJson(
			Answers decisionSupportResponse) {

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Answer.class, new AnswerAdapter());

		Gson gson = builder.create();

		return gson.toJson(decisionSupportResponse);

	}

	public final static ReasoningRequest resoningRequestFronJson(String request) {
		GsonBuilder builder = new GsonBuilder();

		builder.registerTypeAdapter(FunctionalParameterValue.class,
				new FunctionalParameterValueAdapter());
		builder.registerTypeAdapter(FunctionalConstraintValue.class,
				new FunctionalConstraintValueAdapter());
		builder.registerTypeAdapter(Answer.class, new AnswerAdapter());

		Gson gson = builder.create();

		return gson.fromJson(request, ReasoningRequest.class);
	}

}
