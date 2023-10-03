package org.aksw.commons.path.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathOps;
import org.aksw.commons.path.json.PathJson.Step;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class PathOpsJson
    implements PathOps<Step, PathJson>, Serializable
{
    private static PathOpsJson INSTANCE = null;

    public static PathOpsJson get() {
        if (INSTANCE == null) {
            synchronized (PathOpsJson.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PathOpsJson();
                }
            }
        }
        return INSTANCE;
    }


    protected Step SELF_TOKEN = Step.self();
    protected Step PARENT_TOKEN = Step.parent();

    @Override
    public PathJson upcast(Path<Step> path) {
        return (PathJson)path;
    }

    @Override
    public List<Step> getBasePathSegments() {
        return Collections.emptyList();
    }

    @Override
    public Comparator<Step> getComparator() {
        return Comparator.naturalOrder();
    }

    @Override
    public PathJson newPath(boolean isAbsolute, List<Step> segments) {
        return new PathJson(this, isAbsolute, segments);
    }

    @Override
    public Step getSelfToken() {
        return SELF_TOKEN;
    }

    @Override
    public Step getParentToken() {
        return PARENT_TOKEN;
    }

    public static JsonArray toJsonArray(Path<Step> path) {
        JsonArray result = new JsonArray();
        for (Step step : path.getSegments()) {
            if (step.isKey()) {
                result.add(step.getKey());
            } else if (step.isIndex()) {
                result.add(step.getIndex());
            }
        }
        return result;
    }

    @Override
    public String toString(PathJson path) {
        JsonArray arr = toJsonArray(path);
        return (path.isAbsolute() ? "/" : "") + arr.toString();
    }

    @Override
    public String toStringRaw(Object path) {
        return toString((PathJson)path);
    }

    @Override
    public PathJson fromString(String rawJsonStr) {
        String jsonStr = rawJsonStr.trim();
        boolean isAbsolute = jsonStr.startsWith("/");
        if (isAbsolute) {
            jsonStr = jsonStr.substring(1);
        }

        Gson gson = new Gson();
        JsonArray arr = gson.fromJson(jsonStr, JsonArray.class);
        List<Step> steps = new ArrayList<>(arr.size());
        for (JsonElement item : arr) {
            JsonPrimitive primitive = item.getAsJsonPrimitive();
            if (primitive.isString()) {
                steps.add(Step.of(primitive.getAsString()));
            } else if (primitive.isNumber()) {
                steps.add(Step.of(primitive.getAsNumber().intValue()));
            }
        }
        return new PathJson(this, isAbsolute, steps);
    }
}
