module.exports = function (api) {
    api.cache(true);

    const presets = [["@babel/preset-env", {
        targets: [">0.25% in JP", "not ie <= 10", "not dead"],
        useBuiltIns: "usage",
        corejs: 3,
    }]];

    return {
        presets
    };
};