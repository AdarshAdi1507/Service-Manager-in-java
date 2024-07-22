package services;


class ServiceInfo {
    private String name;
    private String displayName;
    private String status;

    public ServiceInfo(String name, String displayName, String status) {
        this.name = name;
        this.displayName = displayName;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        if ("1".equals(status)) {
            return "Stopped";
        } else if ("4".equals(status)) {
            return "Running";
        } else {
            return "Waiting"; // Handle other status values if needed
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public

 String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName + " - " + status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ServiceInfo that = (ServiceInfo) obj;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
