package de.swa.nlq_gc;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class GraphCodeDTO {

    private State state;
    private final String model;
    private List<String> dictionary;
    private int[][] matrix;
    private String error;
    private String description;
    private final long start;

    public GraphCodeDTO(final long start, final String description, final String error, final int[][] matrix,
                        final List<String> dictionary, final String model, final State state) {
        this.start = start;
        this.description = description;
        this.error = error;
        this.matrix = matrix;
        this.dictionary = dictionary;
        this.model = model;
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public List<String> getDictionary() {
        return dictionary;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        ToStringBuilder creator = new ToStringBuilder(this);
        creator.append("state", state)
                .append("start", start)
                .append("model", model)
                .append("dictionary", dictionary)
                .append("matrix", matrix)
                .append("description", description)
                .append("error", error);
        return creator.toString();
    }

}