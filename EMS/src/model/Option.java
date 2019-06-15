package model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Option {

	@SerializedName("value")
	@Expose
	public String value;
	@SerializedName("label")
	@Expose
	public String label;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "Option [value=" + value + ", label=" + label + "]";
	}

}