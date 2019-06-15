package model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item {

	@SerializedName("link")
	@Expose
	public String link;
	@SerializedName("state")
	@Expose
	public String state;
	@SerializedName("editable")
	@Expose
	public Boolean editable;
	@SerializedName("type")
	@Expose
	public String type;
	@SerializedName("name")
	@Expose
	public String name;
	@SerializedName("label")
	@Expose
	public String label;
	@SerializedName("category")
	@Expose
	public String category;
	@SerializedName("tags")
	@Expose
	public List<String> tags = null;
	@SerializedName("groupNames")
	@Expose
	public List<String> groupNames = null;
	@SerializedName("stateDescription")
	@Expose
	public StateDescription stateDescription;
	@SerializedName("members")
	@Expose
	public List<Object> members = null;

	@Override
	public String toString() {
		return "Item [link=" + link + ", state=" + state + ", editable=" + editable + ", type=" + type + ", name="
				+ name + ", label=" + label + ", category=" + category + ", tags=" + tags + ", groupNames=" + groupNames
				+ ", stateDescription=" + stateDescription + ", members=" + members + "]";
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<String> getGroupNames() {
		return groupNames;
	}

	public void setGroupNames(List<String> groupNames) {
		this.groupNames = groupNames;
	}

	public StateDescription getStateDescription() {
		return stateDescription;
	}

	public void setStateDescription(StateDescription stateDescription) {
		this.stateDescription = stateDescription;
	}

	public List<Object> getMembers() {
		return members;
	}

	public void setMembers(List<Object> members) {
		this.members = members;
	}
}