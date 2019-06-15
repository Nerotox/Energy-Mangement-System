package model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StateDescription {

	@SerializedName("pattern")
	@Expose
	public String pattern;
	@SerializedName("readOnly")
	@Expose
	public Boolean readOnly;
	@SerializedName("options")
	@Expose
	public List<Option> options = null;
	@SerializedName("minimum")
	@Expose
	public Integer minimum;
	@SerializedName("maximum")
	@Expose
	public Integer maximum;

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	public Integer getMinimum() {
		return minimum;
	}

	public void setMinimum(Integer minimum) {
		this.minimum = minimum;
	}

	public Integer getMaximum() {
		return maximum;
	}

	public void setMaximum(Integer maximum) {
		this.maximum = maximum;
	}

	@Override
	public String toString() {
		return "StateDescription [pattern=" + pattern + ", readOnly=" + readOnly + ", options=" + options + ", minimum="
				+ minimum + ", maximum=" + maximum + "]";
	}

}
