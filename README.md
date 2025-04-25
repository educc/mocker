# mocker

This project provides a simple HTTP mocking service built with Java and Vert.x.

## Building the Image

To build the container image using Podman, navigate to the project's root directory and run:

```bash
podman build -t mocker .
```

This command uses the provided `Dockerfile` to build an image tagged as `mocker`.

## Running the Container

To run the mocker service using Podman:

```bash
podman run --rm -p 9090:9090 -v ./path/to/your/local/mock/data:/data/mock --name my-mocker mocker 9090 /data/mock
```

**Explanation:**

*   `--rm`: Automatically remove the container when it exits.
*   `-p 9090:9090`: Maps port 9090 on your host machine to port 9090 inside the container (the port the application listens on).
*   `-v ./path/to/your/local/mock/data:/data/mock:Z`: Mounts a local directory containing your mock definition files (`.json`) into the `/data/mock` directory inside the container. 
    *   **Important:** Replace `./path/to/your/local/mock/data` with the actual path to your mock data directory on your host machine.
*   `--name my-mocker`: Assigns a name to the running container for easier management.
*   `mocker`: Specifies the image to run.
*   `9090`: The first argument passed to the application, specifying the port to listen on inside the container.
*   `/data/mock`: The second argument passed to the application, specifying the path *inside the container* where the mock definition files are located.

The service will then be accessible at `http://localhost:9090`.