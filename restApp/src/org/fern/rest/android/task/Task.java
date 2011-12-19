package org.fern.rest.android.task;

import org.fern.rest.android.Duration;
import org.fern.rest.android.RestApplication.Priority;

import java.io.StringWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.xmlpull.v1.XmlSerializer;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.Xml;

public class Task implements Parcelable, Comparable<Task> {
    private String mName = null;
    private URI mUri = null;
    private String mStatus = null;
    private String mType = null;
    private Priority mPriority = Priority.NONE;
    private Integer mProgress = null;
    private Integer mProcessProgress = null;
    private List<Task> mParents = new ArrayList<Task>();
    private List<Task> mChildren = new ArrayList<Task>();
    private Set<String> mTags = new HashSet<String>();
    private Date mAdditionDate = null;
    private Date mModificationDate = null;
    private Date mActivationDate = null;
    private Date mExpirationDate = null;
    private Duration mEstimatedCompletionTime = null;
    private String mDetails = null;
    private long mKeyID = -1;
    private String mEtag;
    private Date mEtagLastUpdated;

    public Task() {
    }
    
    public String toString() {
        return getName() == null ? "" : getName();
    }

    private static final SimpleDateFormat outFmt = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());

    private String formatDate(Date date) {
        String dateStr = outFmt.format(date);
        dateStr = dateStr.substring(0, dateStr.length() - 2) + ":00";
        return dateStr;
    }

    public String toXML() {
        String xml = null;
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "tm");
            serializer.attribute("", "xmlns",
                    "http://danieloscarschulte.de/cs/ns/2011/tm");
            serializer.startTag("", "taskDescription");
            if (this.mUri != null) serializer.attribute("", "self", this
                    .getURI().toString());

            serializer.startTag("", "taskName");
            serializer.text(this.getName());
            serializer.endTag("", "taskName");

            serializer.startTag("", "taskType");
            serializer.text(this.getName());
            serializer.endTag("", "taskType");

            serializer.startTag("", "taskDetail");
            serializer.attribute("", "type", "text/plain");
            serializer.text(this.getDetails());
            serializer.endTag("", "taskDetail");

            serializer.startTag("", "taskStatus");
            serializer.text(this.getStatus());
            serializer.endTag("", "taskStatus");

            serializer.startTag("", "taskPriority");
            serializer.text(this.getPriority().toString());
            serializer.endTag("", "taskPriority");

            if (this.mActivationDate != null) {
                serializer.startTag("", "taskActivationTime");
                serializer.text(formatDate(getActivatedDate()));
                serializer.endTag("", "taskActivationTime");
            }

            if (this.mExpirationDate != null) {
                serializer.startTag("", "taskExpirationTime");
                serializer.text(formatDate(getExpirationDate()));
                serializer.endTag("", "taskExpirationTime");
            }

            if (this.mAdditionDate != null) {
                serializer.startTag("", "taskAdditionTime");
                serializer.text(formatDate(getAdditionDate()));
                serializer.endTag("", "taskAdditionTime");
            }

            if (this.mModificationDate != null) {
                serializer.startTag("", "taskModificationTime");
                serializer.text(formatDate(getModifiedDate()));
                serializer.endTag("", "taskModificationTime");
            }

            if (this.mEstimatedCompletionTime != null) {
                serializer.startTag("", "estimatedDuration");
                serializer.text(mEstimatedCompletionTime.toString());
                serializer.endTag("", "estimatedDuration");
            }

            if (this.getProgress() != null) {
                serializer.startTag("", "taskProgress");
                serializer.text(Integer.toString(this.getProgress()));
                serializer.endTag("", "taskProgress");
            }

            if (this.getProcessProgress() != null) {
                serializer.startTag("", "processProgress");
                serializer.text(Integer.toString(this.getProcessProgress()));
                serializer.endTag("", "processProgress");
            }

            Set<String> tags = this.getTags();
            for (String tag: tags) {
                serializer.startTag("", "taskTag");
                serializer.text(tag);
                serializer.endTag("", "taskTag");
            }

            serializer.endTag("", "taskDescription");
            serializer.endTag("", "tm");
            serializer.endDocument();

            xml = writer.toString();
            return xml;
        } catch (Exception e) {
            Log.e("Tag to xml", "error building xml representation");
        }

        return xml;
    }

    public Task(String name, URI uri) {
        this.setName(name);
        this.setURI(uri);
    }

    public void setKeyID(long key) {
        mKeyID = key;
    }

    public long getKeyID() {
        return mKeyID;
    }

    public Task(String name, URI uri, String status, String type,
            Priority priority, Integer progress, Integer processProgress,
            List<Task> parents, List<String> tags, Date additionDate,
            Date modificationDate, Date activationDate, Date expirationDate,
            Duration estimatedCompletionTime) {
        this(name, uri);
        this.setStatus(status);
        this.setType(type);
        this.setPriority(priority);
        this.setProgress(progress);
        this.setProcessProgress(processProgress);
        this.mParents.clear();
        this.addAllParents(parents);
        this.mTags.clear();
        this.addAllTags(tags);
        this.setAdditionDate(additionDate);
        this.setModifiedDate(modificationDate);
        this.setActivatedDate(activationDate);
        this.setExpirationDate(expirationDate);
        this.setEstimatedCompletionTime(estimatedCompletionTime);
    }

    public URI getURI() {
        return mUri;
    }

    public void setURI(URI uri) {
        this.mUri = uri;
    }

    public void setURI(String uri) {
        this.mUri = URI.create(uri);
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        this.mStatus = status;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Priority getPriority() {
        return mPriority;
    }

    public void setPriority(Priority priority) {
        this.mPriority = priority;
    }

    public void setPriority(String priority) {
        this.mPriority = Priority.valueOf(priority);
    }

    public void setPriority(int priority) {
        this.mPriority = Priority.values()[priority];
    }

    public Integer getProgress() {
        return mProgress;
    }

    public void setProgress(Integer progress) {
        if (progress < 0 || progress > 100) {
            this.mProgress = null;
        } else {
            this.mProgress = progress;
        }
    }

    public Integer getProcessProgress() {
        return mProcessProgress == null ? 0 : mProcessProgress;
    }

    public void setProcessProgress(Integer processProgress) {
        if (processProgress < 0 || processProgress > 100) {
            this.mProcessProgress = null;
        } else {
            this.mProcessProgress = processProgress;
        }
    }

    public List<Task> getParents() {
        return this.mParents;
    }

    public void addParent(Task parent) {
        this.mParents.add(parent);
    }

    public void addAllParents(List<Task> parentList) {
        this.mParents.addAll(parentList);
    }

    public void removeParent(Task task) {
        this.mParents.remove(task);
    }

    public void clearParents() {
        this.mParents.clear();
    }

    public List<Task> getChildren() {
        return null;
    }

    public Set<String> getTags() {
        return this.mTags;
    }

    public void addAllTags(List<String> tags) {
        this.mTags.addAll(formatTags(tags));
    }

    public void clearTags() {
        this.mTags.clear();
    }

    public void linkTag(String tag) {
        this.mTags.add(formatTag(tag));
    }

    public void unlinkTag(String tag) {
        this.mTags.remove(formatTag(tag));
    }

    public Date getAdditionDate() {
        return mAdditionDate;
    }

    public void setAdditionDate(Date date) {
        this.mAdditionDate = date;
    }

    public void setAdditionDate(Long date) {
        if (date < 0) {
            this.mAdditionDate = null;
        } else {
            this.mAdditionDate = new Date(date);
        }
    }

    public Date getModifiedDate() {
        return this.mModificationDate;
    }

    public void setModifiedDate(Date date) {
        this.mModificationDate = date;
    }

    public void setModifiedDate(Long date) {
        if (date < 0) {
            this.mModificationDate = null;
        } else {
            this.mModificationDate = new Date(date);
        }
    }

    public void setActivatedDate(Date date) {
        this.mActivationDate = date;
    }

    public void setActivatedDate(Long date) {
        if (date < 0) {
            this.mActivationDate = null;
        } else {
            this.mActivationDate = new Date(date);
        }
    }

    public Date getActivatedDate() {
        return this.mActivationDate;
    }

    public void setExpirationDate(Date date) {
        this.mExpirationDate = date;
    }

    public void setExpirationDate(Long date) {
        if (date < 0) {
            this.mExpirationDate = null;
        } else {
            this.mExpirationDate = new Date(date);
        }
    }

    public Date getExpirationDate() {
        return this.mExpirationDate;
    }

    public void setEstimatedCompletionTime(Duration duration) {
        this.mEstimatedCompletionTime = duration;
    }

    public void setEstimatedCompletionTime(String duration) {
        this.mEstimatedCompletionTime = Duration.parse(duration);
    }

    public Duration getEstimatedCompletionTime() {
        return this.mEstimatedCompletionTime;
    }

    public void setDetails(String details) {
        this.mDetails = details;
    }

    public String getDetails() {
        return mDetails;
    }

    public void setEtag(String etag) {
        this.mEtag = etag;
    }

    public String getEtag() {
        return this.mEtag;
    }

    public void setEtagUpdateTime(Date date) {
        this.mEtagLastUpdated = date;
    }

    public Date getLastEtagUpdate() {
        return this.mEtagLastUpdated;
    }

    public void addTag(String tag) {
        this.mTags.add(formatTag(tag));
    }

    public boolean containsAnyTag(List<String> tagsFilter) {
        List<String> tags = new ArrayList<String>(this.getTags());
        if (tagsFilter == null || tagsFilter.size() == 0) {
            return true;
        } else {
            tags.retainAll(formatTags(tagsFilter));
            return tags.size() != 0;
        }
    }

    @Override
    public int compareTo(Task other) {
        return this.getName().compareTo(other.getName());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.getName());
        out.writeString(this.getURI().toString());
        out.writeString(this.getStatus());
        out.writeString(this.getType());
        out.writeString(this.getPriority().toString());
        out.writeString(this.getDetails());
        out.writeString(this.getEstimatedCompletionTime() == null ? null : this
                .getEstimatedCompletionTime().toString());
        out.writeInt(this.getProgress() == null ? -1 : this.getProgress());
        out.writeInt(this.getProcessProgress() == null ? -1 : this
                .getProcessProgress());
        out.writeLong(this.getAdditionDate() == null ? -1 : this
                .getAdditionDate().getTime());
        out.writeLong(this.getModifiedDate() == null ? -1 : this
                .getModifiedDate().getTime());
        out.writeLong(this.getActivatedDate() == null ? -1 : this
                .getActivatedDate().getTime());
        out.writeLong(this.getExpirationDate() == null ? -1 : this
                .getExpirationDate().getTime());
        out.writeList(this.getParents());
        out.writeList(this.getChildren());
        out.writeStringList(new ArrayList<String>(this.getTags()));
        out.writeString(this.getEtag());
        if ( this.getLastEtagUpdate() != null)
        	out.writeLong(this.getLastEtagUpdate().getTime());
    }

    public Task(Parcel in) {
        this.setName(in.readString());
        this.setURI(URI.create(in.readString()));
        this.setStatus(in.readString());
        this.setType(in.readString());
        this.setPriority(in.readString());
        this.setDetails(in.readString());
        this.setEstimatedCompletionTime(in.readString());
        this.setProgress(in.readInt());
        this.setProcessProgress(in.readInt());
        this.setAdditionDate(in.readLong());
        this.setModifiedDate(in.readLong());
        this.setActivatedDate(in.readLong());
        this.setExpirationDate(in.readLong());
        in.readList(mParents, Task.class.getClassLoader());
        in.readList(mChildren, Task.class.getClassLoader());
        List<String> tagList = new ArrayList<String>();
        in.readStringList(tagList);
        mTags = new HashSet<String>(tagList);
        this.setEtag(in.readString());
        long d = in.readLong();// get etag update time if valid
        if ( d != 0)
        	this.setEtagUpdateTime(new Date(d));
    }

    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
    
    public static List<String> formatTags(List<String> tags) {
        List<String> out = new ArrayList<String>(tags.size());
        for (String tag: tags) {
        	if ( tag.trim().length() != 0 )
        		out.add(tag.trim());
            //out.add(formatTag(tag));
        }
        return out;
    }
    
    public static String formatTag(String tag) {
        // Make the tag lower case, and replace spaces with hyphens
        tag = tag.toLowerCase().replaceAll("\\s+", "-");
        // Remove all extra symbols (leave umlauted chars, etc).
        return tag.replaceAll("[^\\p{L}\\p{N}]", "");
    }
}
