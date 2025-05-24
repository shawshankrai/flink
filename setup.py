from setuptools import setup, find_packages

setup(
    name="flink-project",
    version="0.1.0",
    packages=find_packages(),
    install_requires=[
        "apache-flink",
        "pyyaml",
    ],
    python_requires=">=3.11",
    include_package_data=True,
    package_data={
        '': ['config/*.yaml'],
    },
) 